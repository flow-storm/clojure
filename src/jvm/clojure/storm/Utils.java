package clojure.storm;

import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentVector;
import clojure.lang.ISeq;
import clojure.lang.LispReader;
import clojure.lang.MapEntry;
import clojure.lang.Namespace;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Symbol;

public class Utils {
	
	public static Object mergeMeta(Object x, IPersistentMap m) {
		if (x instanceof clojure.lang.IObj) {
			IObj o = (IObj) x;
			if (m != null || RT.meta(o) != null)
				{
				IPersistentMap retMeta = PersistentHashMap.EMPTY;
				IPersistentMap oMeta = RT.meta(o);

				if (m != null)
					{
					for (Object meo : m)
						{
						MapEntry me = (MapEntry) meo;
						retMeta = retMeta.assoc(me.getKey(), me.getValue());
						}
					}
				if (oMeta != null)
					{
					for (Object meo : oMeta)
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
		if (sym.getNamespace() != null &&
			sym.getNamespace().equals("clojure.core") &&
			sym.getName().equals("addMethod"))
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

	public static IObj addCoordMeta(IObj o) {
		
	Object meta = RT.meta(o);
	IPersistentVector coord = (IPersistentVector) LispReader.COORD.deref();
	if(coord != null)
		{
		meta = RT.assoc(meta, LispReader.COORD_KEY, coord);
		return o.withMeta((IPersistentMap)meta);
		} else {
		return o;
		}
}



	}
