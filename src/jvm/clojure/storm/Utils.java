package clojure.storm;

import clojure.lang.IObj;
import clojure.lang.IPersistentCollection;
import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentList;
import clojure.lang.IPersistentVector;
import clojure.lang.IPersistentSet;
import clojure.lang.IRecord;
import clojure.lang.ISeq;
import clojure.lang.IFn;
import clojure.lang.AFn;
import clojure.lang.LispReader;
import clojure.lang.MapEntry;
import clojure.lang.Keyword;
import clojure.lang.Namespace;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentList;
import clojure.lang.PersistentVector;
import clojure.lang.PersistentHashSet;
import clojure.lang.PersistentTreeSet;
import clojure.lang.RT;
import clojure.lang.Symbol;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class Utils {
	
	public static Object mergeMeta(Object x, IPersistentMap m) {
		if (x instanceof clojure.lang.IObj) {
			IObj o = (IObj) x;
			if (m != null || RT.meta(o) != null)
				{ // if there is meta in any of the args
				IPersistentMap retMeta = PersistentHashMap.EMPTY;
				IPersistentMap oMeta = RT.meta(o);

				if (oMeta != null)
					{
					for (Object meo : oMeta)
					{
						MapEntry me = (MapEntry) meo;
						retMeta = retMeta.assoc(me.getKey(), me.getValue());
						}
					}
					// m meta overrides the input Object meta when both have
					if (m != null)
					{
						for (Object meo : m)
						{
							MapEntry me = (MapEntry) meo;
							retMeta = retMeta.assoc(me.getKey(), me.getValue());
						}
					}
				return o.withMeta(retMeta);
				} else {
				return x;
				}
			}
		return x;
		}

	public static Symbol maybeGetTraceSymbol(Symbol sym, ISeq form){
        // If the form is the expansion of a defmethod we use the symbol of
        // the defmethod as a trace symbol.
        // There are rare cases (flow-storm-debugger/issues/90) where defmethod can be
        // used without a symbol there, but we skip them for now since they are very rare
		if (sym.getNamespace() != null &&
			sym.getNamespace().equals("clojure.core") &&
			sym.getName().equals("addMethod") &&
            RT.second(form) instanceof Symbol)
			{
            
			Symbol nameSym = (Symbol) RT.second(form);
			String currentNsName = ((Namespace)RT.CURRENT_NS.deref()).getName().getName();
			return  Symbol.create(nameSym.getNamespace() != null ? nameSym.getNamespace() : currentNsName,
				nameSym.getName());
			} else {
			return null;
			}
	}

	public static IPersistentVector coordOf(Object form) {
		IPersistentVector coord = (IPersistentVector) RT.get(RT.meta(form), LispReader.COORD_KEY);
				
		if(coord == null &&
			form != null &&
			form instanceof ISeq &&
			((ISeq)form).count() > 0)
			{
			// If the form is a list and has no coord, maybe it was
			// destroyed by a macro. Try guessing the coord by looking at
			// the first element. This fixes `->`, for instance.
			coord = (IPersistentVector) RT.get(RT.meta(((ISeq)form).first()), LispReader.COORD_KEY);
			}
		return coord;
	}

	public static Object addCoordMeta(Object o, IPersistentVector coord) {
		
	Object meta = RT.meta(o);    
	if(coord != null && o instanceof IObj)
		{
		meta = RT.assoc(meta, LispReader.COORD_KEY, coord);
		return ((IObj)o).withMeta((IPersistentMap)meta);
		} else {
		return o;
		}
	}

    public static long clojureFormSourceHash(String s) {
        long M = 4294967291L;
        String cleanS = s.replaceAll("#[/.a-zA-Z0-9_-]+", "") // remove tags
            .replaceAll("\\^:[a-zA-Z0-9_-]+","")              // remove meta keys
            .replaceAll("\\^\\{.+?\\}","")                    // remove meta maps
            .replaceAll(";.+\n","")                           // remove comments
            .replaceAll("[ \t\n]+","");                       // remove non visible
        long sum = 0;
        long mul = 1;
        int i = 0;

        while (i < cleanS.length()) {
            int cval = (int)cleanS.charAt(i);
            mul = ((i % 4) == 0)? 1 : mul * 256;
            sum = sum + cval*mul;
            i++;
        }

        return sum % M;
    }

    public static String objCoord(String kind, Object form) {
        long hash = (form == null)? 0 : clojureFormSourceHash(form.toString());
        return kind + hash;
    }
    
    private static IPersistentCollection mapIndexed(IFn f, IPersistentCollection coll) {
        List<Object> objs = new ArrayList<Object>();
        
        int i = 0;
        Iterator<Object> iter = RT.iter(coll);
        
        while(iter.hasNext()) {
            Object o = iter.next();
            Object mappedO = f.invoke(i, o);
            objs.add(mappedO);
            i++;
        }
        if (coll instanceof PersistentVector) {
            return PersistentVector.create(objs);
        } else if (coll instanceof PersistentHashSet) {
            return PersistentHashSet.create(objs);
        } else {
            return PersistentList.create(objs);
            
        }                
    }
    
    private static Object walkCodeForm(IPersistentVector coord, IFn f, Object form) {
        IFn walkCollection = new AFn() {
		               public Object invoke(Object forms) {
                           return mapIndexed(new AFn() {
                               public Object invoke(Object idx, Object frm) {
                                   return walkCodeForm((IPersistentVector) RT.conj(coord, idx), f, frm);
                               }
                               },
                               (IPersistentCollection)forms);
		               }
        };
        IFn walkSet = new AFn() {
		               public Object invoke(Object forms) {
                           return (IPersistentSet) mapIndexed(new AFn() {
                               public Object invoke(Object idx, Object frm) {                        
                                   return walkCodeForm((IPersistentVector) RT.conj(coord, objCoord("K", frm)), f, frm);
                               }
                               },
                               (IPersistentSet)forms);
		               }
        };

        IFn walkMap = new AFn() {
            public Object invoke(Object m) {
                List<Object> kvs = new ArrayList<Object>();
                Iterator<Object> iter = RT.iter(m);
                while(iter.hasNext()) {
                    MapEntry e = (MapEntry) iter.next();
                    Object kfrm = e.getKey();
                    Object vfrm = e.getValue();
                    kvs.add(walkCodeForm((IPersistentVector) RT.conj(coord, objCoord("K", kfrm)), f, kfrm));
                    kvs.add(walkCodeForm((IPersistentVector) RT.conj(coord, objCoord("V", kfrm)), f, vfrm));
                }
                Object[] r = kvs.toArray();
                return RT.map(r);
                }
            };

        Object result = null;
        if ((form instanceof IPersistentMap) && !(form instanceof IRecord)) {
            result = walkMap.invoke(form);
        } else if (form instanceof IPersistentSet && !(form instanceof PersistentTreeSet)) {
            result = walkSet.invoke(form);  
        } else if (form instanceof IPersistentCollection && !(form instanceof IRecord) && !(form instanceof MapEntry)) {
            result = walkCollection.invoke(form);
        } else {
                result = form;
        }
                
        return f.invoke(coord, mergeMeta(result, RT.meta(form)));
    }

    public static Object tagFormRecursively(Object form) {
        return walkCodeForm(
            PersistentVector.EMPTY,
            new AFn() {            
            public Object invoke(Object coord, Object frm) {
                if ((frm instanceof clojure.lang.ISeq) || (frm instanceof clojure.lang.Symbol))
                    return addCoordMeta(frm, (IPersistentVector)coord);
                else
                    return frm;
            }
            },
            form
            );
    }

    static public Object tagStormCoord(Object form) {
        boolean dontTag = (form!=null &&
            RT.meta(form)!=null &&
            RT.get(RT.meta(form), Keyword.intern("clojure.storm", "dont-tag")) != null);
        
        if((Boolean)clojure.storm.Emitter.INSTRUMENTATION_ENABLE.deref() && !dontTag) {
            try {            
                Object tagged = Utils.tagFormRecursively(form);            
                return tagged;
                } catch (Throwable e) {                
                e.printStackTrace();
                return form;
                }                
            }        
        else
            return form;
        }


    public static Object stripStormMeta(Object form) {
        return walkCodeForm(
            PersistentVector.EMPTY,
            new AFn() {            
                public Object invoke(Object coord, Object frm) {
                    if ((frm instanceof clojure.lang.ISeq) || (frm instanceof clojure.lang.Symbol)){
                        IObj mfrm = (IObj) frm;
                        IPersistentMap frmMeta = RT.meta(mfrm);
                        return mfrm.withMeta((IPersistentMap) RT.dissoc(frmMeta, LispReader.COORD_KEY));                        
                    } else
                        return frm;
                    }
                },
            form
            );
        }

	public static int toInt(Object n) {
		if (n == null)                 return 0;
		else if (n instanceof Integer) return (int) n;
		else if (n instanceof Long)    return ((Long) n).intValue();
		else return 0;
	} 



	}
