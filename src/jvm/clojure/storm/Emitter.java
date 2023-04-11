package clojure.storm;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import clojure.asm.Type;
import clojure.asm.commons.GeneratorAdapter;
import clojure.asm.commons.Method;
import clojure.lang.AFn;
import clojure.lang.Compiler;
import clojure.lang.Compiler.BindingInit;
import clojure.lang.Compiler.FnExpr;
import clojure.lang.Compiler.FnMethod;
import clojure.lang.Compiler.LocalBinding;
import clojure.lang.Compiler.ObjExpr;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.Namespace;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public class Emitter {

	final static Type TRACER_CLASS_TYPE = Type.getType(Tracer.class);
	final static Type OBJECT_CLASS_TYPE = Type.getType(Object.class);    
	final static Type INT_TYPE = Type.getType(int.class);
	final static Type INTEGER_CLASS_TYPE = Type.getType(Integer.class);
	final static Type LONG_CLASS_TYPE = Type.getType(Long.class);
	final static Type DOUBLE_CLASS_TYPE = Type.getType(Double.class);
	
	static Keyword LINE_KEY = Keyword.intern(null, "line");
	static Keyword NS_KEY = Keyword.intern(null, "ns");
    
	public static Var INSTRUMENTATION_ENABLE = Var.create(false).setDynamic();

	private static ArrayList<String> instrumentationSkipPrefixes = new ArrayList();
	private static ArrayList<String> instrumentationOnlyPrefixes = new ArrayList();

	static {	
		String instrumentationEnableProp = System.getProperty("clojure.storm.instrumentEnable");
		if(instrumentationEnableProp != null)
			setInstrumentationEnable(Boolean.parseBoolean(instrumentationEnableProp)); 
									
        String skipPrefixesProp = System.getProperty("clojure.storm.instrumentSkipPrefixes"); 
		if(skipPrefixesProp != null)
			{
				String[] prefixes = skipPrefixesProp.split(",");
				for(String p : prefixes)
					addInstrumentationSkipPrefix(Compiler.munge(p));
					
			}

		String onlyPrefixesProp = System.getProperty("clojure.storm.instrumentOnlyPrefixes");
		if(onlyPrefixesProp != null)
			{
				String[] prefixes = onlyPrefixesProp.split(",");
				for(String p : prefixes)
					addInstrumentationOnlyPrefix(Compiler.munge(p));
					
			}
		if(instrumentationOnlyPrefixes.size()>0 && instrumentationSkipPrefixes.size()>0)
			{
			System.out.println("Warning, instrumentOnlyPrefixes and instrumentSkipPrefixes are both set, only instrumentOnlyPrefixes will be taken into account.");
			}
		}
	
	public static void setInstrumentationEnable(Boolean x) {
		IFn f = new AFn() {
			public Object invoke(Object prev) {
				return x;
			}
		};
		INSTRUMENTATION_ENABLE.alterRoot(f ,null);        
		System.out.println("Storm instrumentation set to: " + INSTRUMENTATION_ENABLE.deref());
	}

	public static Boolean getInstrumentationEnable() {
		return (Boolean) INSTRUMENTATION_ENABLE.deref();
	}

	public static ArrayList<String> getInstrumentationSkipPrefixes() {
		return instrumentationSkipPrefixes;
	}

	public static String makePrefixesString(ArrayList<String> prefixes) {
		if(prefixes.size()>0)
			{
			List prefs = prefixes.stream().map(p -> Compiler.demunge(p)).collect(Collectors.toList());
            return String.join(",", prefs);
			} else {
			return null;
			}        
	}

	public static ArrayList<String> getInstrumentationOnlyPrefixes() {
		return instrumentationOnlyPrefixes;
	}
	
	public static void addInstrumentationSkipPrefix(String prefix) {
		instrumentationSkipPrefixes.add(prefix);
	}

	public static void addInstrumentationOnlyPrefix(String prefix) {
		instrumentationOnlyPrefixes.add(prefix);        
	}
	
	public static boolean skipInstrumentation(String fqFnName) {        

		boolean instrumentBecasueOfPrefixes = false;
		if(instrumentationOnlyPrefixes.size() > 0)
			{
			// if any of the only is true, make instrumentBecasueOfPrefixes true
			for (String prefix : instrumentationOnlyPrefixes)
				{
				instrumentBecasueOfPrefixes |= fqFnName.startsWith(prefix);
				}            

			}
		else
			{
			instrumentBecasueOfPrefixes = true;
			// if any of the skip is true, make instrumentBecasueOfPrefixes false            
			for (String prefix : instrumentationSkipPrefixes)
				{
				instrumentBecasueOfPrefixes &= !fqFnName.startsWith(prefix);
				}
			}
		boolean skip = !getInstrumentationEnable() || !instrumentBecasueOfPrefixes;
        return skip;
	}
	
	private static void dupAndBox(GeneratorAdapter gen, Type t) {
		if(t != null && (Type.LONG_TYPE.equals(t) || Type.DOUBLE_TYPE.equals(t))) {
			gen.dup2();
			gen.valueOf(t);
		} else if (t != null
				   && (Type.INT_TYPE.equals(t) ||
					   Type.BYTE_TYPE.equals(t) ||
					   Type.FLOAT_TYPE.equals(t) ||
					   Type.BOOLEAN_TYPE.equals(t) ||
					   Type.CHAR_TYPE.equals(t) ||
					   Type.SHORT_TYPE.equals(t))) {
			gen.dup();
			gen.valueOf(t);
		} else {
			gen.dup();
		}	
	}
	
	public static void emitFnCallTrace(GeneratorAdapter gen, ObjExpr fn, FnMethod method, Type[] argtypes, IPersistentVector arglocals) {

		boolean skipFn = skipInstrumentation(fn.name()) || method.skipFnCallTrace;
		if (!skipFn) {
			int formId = (Integer) Compiler.FORM_ID.deref();            
			Symbol name = (method.methodTraceSymbol == null) ? Symbol.create(Compiler.demunge(fn.name())) : method.methodTraceSymbol;
			String fnName = name.getName();
			String fnNs = name.getNamespace();

			gen.loadArgArray();
			gen.invokeStatic(Type.getType(clojure.lang.PersistentVector.class), Method.getMethod("clojure.lang.PersistentVector create(Object[])"));
            
			gen.push(fnNs);
			gen.push(fnName);
			gen.push(formId);
			gen.invokeStatic(TRACER_CLASS_TYPE, Method.getMethod("void traceFnCall(clojure.lang.IPersistentVector, String, String, int)"));

			emitBindTraces(gen, fn, arglocals, PersistentVector.EMPTY);                                       
		}            
	}    
 
	public static void emitFnReturnTrace(GeneratorAdapter gen, ObjExpr fn, FnMethod method, Type retType) {
		
		boolean skipFn = skipInstrumentation(fn.name()) || method.skipFnCallTrace;
		if (!skipFn) {
			int formId = (Integer) Compiler.FORM_ID.deref();
			if(Type.LONG_TYPE.equals(retType) || Type.DOUBLE_TYPE.equals(retType)) {
				gen.dup2();
				gen.valueOf(retType);		
			}  else {
				gen.dup();			
			}

			emitCoord(gen, fn.getCoord());
				
			gen.push(formId);
			gen.invokeStatic(TRACER_CLASS_TYPE, Method.getMethod("void traceFnReturn(Object, Integer[], int)"));
		}
	}

	private static void emitCoord(GeneratorAdapter gen, IPersistentVector coord) {

		if (coord == null) {
			coord =  PersistentVector.EMPTY;
		}

		gen.push(coord.count());
		gen.newArray(INTEGER_CLASS_TYPE);
		for (int i = 0; i < coord.count(); i++) {
			gen.dup();
			Object cio = coord.nth(i);
			Integer ci = null;
			if      (cio instanceof Integer) ci = (Integer) cio;
			else if (cio instanceof Long)    ci = ((Long) cio).intValue();

			gen.push(i);
			gen.push(ci);            
			gen.box(INT_TYPE);
			gen.arrayStore(INTEGER_CLASS_TYPE);
		}        
	}	

	public static void emitExprTrace(GeneratorAdapter gen, ObjExpr objx, IPersistentVector coord, Type retType) {
		if (coord != null) {
			
			int formId = (Integer)Compiler.FORM_ID.deref();
                
			if (objx instanceof FnExpr && !skipInstrumentation(((FnExpr)objx).name())) {
				//System.out.println("@@@@ doing it for " + ((FnExpr) objx).name() + coord);
				// assumes the stack contains the value to be traced
				// duplicate the value for tracing, so we don't consume it
				dupAndBox(gen, retType);
								
				emitCoord(gen, coord);

				gen.push(formId);
		
				// trace
				gen.invokeStatic(TRACER_CLASS_TYPE,Method.getMethod("void traceExpr(Object, Integer[], int)"));
			}		
		}
		
	}

	public static void emitBindTrace(GeneratorAdapter gen, ObjExpr objx, BindingInit bi, IPersistentVector coord) {
		String symName = Compiler.demunge(bi.binding().name);
		Integer bIdx = bi.binding().idx;
		
		if (objx instanceof FnExpr &&
			!skipInstrumentation(((FnExpr)objx).name()) &&
			coord != null &&
			!symName.equals("_")) {					

			Type valType = null;
			Class primc = Compiler.maybePrimitiveType(bi.init());
			if (primc != null) valType = Type.getType(primc);

			// assume binding val on the stack
			dupAndBox(gen, valType);
			emitCoord(gen, coord);
			gen.push(symName);

			gen.invokeStatic(TRACER_CLASS_TYPE,
				Method.getMethod("void traceBind(Object, Integer[], String)"));

		}
	}
	 
	public static void emitBindTraces(GeneratorAdapter gen, ObjExpr objx, IPersistentVector localBindings,
			IPersistentVector coord) {

		if (objx instanceof FnExpr && !skipInstrumentation(((FnExpr) objx).name())) {
            
			int formId = (Integer) Compiler.FORM_ID.deref();

			for (int i = 0; i < localBindings.count(); i++) {

				LocalBinding lb = (LocalBinding) localBindings.nth(i);

				String symName = Compiler.demunge(lb.name);

				if (coord != null && !symName.equals("_") && lb.used) {
					
					objx.emitLocal(gen, lb, false, null);
					
					emitCoord(gen, coord);
					gen.push(symName);

					gen.invokeStatic(TRACER_CLASS_TYPE,
						Method.getMethod("void traceBind(Object, Integer[], String)"));
				}
			}
		}
	}

	public static void emitFormsRegistration(GeneratorAdapter gen, List<Object> forms, String sourcePath) {
		for (Object form : forms) {
			IPersistentMap fmeta = RT.meta(form);
			if (fmeta != null) {
				int fline = -1;
				// when using the decompiler this is being returned as a Long and throwing a cast error
				// so lets hack it like this
				Object oline = fmeta.valAt(LINE_KEY);
				if (oline instanceof Integer) {
					fline = (Integer) oline;
				}

				Namespace ns = (Namespace) RT.CURRENT_NS.deref();
				
				String fns = ns.getName().toString();
                
				int fid = form.hashCode();
				gen.push(fid);
				gen.push(fline);
				gen.push(fns);
				gen.push(sourcePath);
				gen.invokeStatic(TRACER_CLASS_TYPE, Method.getMethod("void registerFormLocation(int, int, String, String)"));
			}

		}
	}
}
