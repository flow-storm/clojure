package clojure.storm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import clojure.asm.Opcodes;
import clojure.asm.Type;
import clojure.asm.Label;
import clojure.asm.commons.GeneratorAdapter;
import clojure.asm.commons.Method;
import clojure.lang.AFn;
import clojure.lang.Compiler;
import clojure.lang.Compiler.BindingInit;
import clojure.lang.Compiler.FnExpr;
import clojure.lang.Compiler.NewInstanceExpr;
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

    private static ArrayList<String> instrumentationOnlyPrefixes = new ArrayList();
	private static ArrayList<String> instrumentationSkipPrefixes = new ArrayList();    
    private static Pattern instrumentationSkipRegex = null;
    
    private static boolean fnCallInstrumentationEnable=true;
    private static boolean fnReturnInstrumentationEnable=true;
    private static boolean exprInstrumentationEnable=true;
    private static boolean bindInstrumentationEnable=true;

    ////////////////////
    // Initialization //
    ////////////////////

    static {	
		String instrumentationEnableProp = System.getProperty("clojure.storm.instrumentEnable");
		if(instrumentationEnableProp != null)
			setInstrumentationEnable(Boolean.parseBoolean(instrumentationEnableProp)); 

        String onlyPrefixesProp = System.getProperty("clojure.storm.instrumentOnlyPrefixes");
		if(onlyPrefixesProp != null)
			{
				String[] prefixes = onlyPrefixesProp.split(",");
				for(String p : prefixes)
					addInstrumentationOnlyPrefix(p);
					
			}
        
        String skipPrefixesProp = System.getProperty("clojure.storm.instrumentSkipPrefixes"); 
		if(skipPrefixesProp != null)
			{
				String[] prefixes = skipPrefixesProp.split(",");
				for(String p : prefixes)
					addInstrumentationSkipPrefix(p);
					
			}

        String skipRegexProp = System.getProperty("clojure.storm.instrumentSkipRegex"); 
		if(skipRegexProp != null)
			instrumentationSkipRegex = Pattern.compile(skipRegexProp, Pattern.CASE_INSENSITIVE);
		}

    /////////////////////////////////////////////////
    // Instrumentation enabling/disabling controls //
    /////////////////////////////////////////////////
    
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

    public static void setFnCallInstrumentationEnable(boolean enable){fnCallInstrumentationEnable=enable;}
    public static void setFnReturnInstrumentationEnable(boolean enable){fnReturnInstrumentationEnable=enable;}
    public static void setExprInstrumentationEnable(boolean enable){exprInstrumentationEnable=enable;}
    public static void setBindInstrumentationEnable(boolean enable){bindInstrumentationEnable=enable;}

	////////////////////////////////////////
    // Namespace instrumentation controls //
    ////////////////////////////////////////

    public static String makePrefixesString(ArrayList<String> prefixes) {
		if(prefixes.size()>0)
			{
			List prefs = prefixes.stream().map(p -> Compiler.demunge(p)).collect(Collectors.toList());
            return String.join(",", prefs);
			} else {
			return null;
			}        
	}

	public static ArrayList<String> getInstrumentationSkipPrefixes() {
		return instrumentationSkipPrefixes;
	}

	public static void addInstrumentationSkipPrefix(String prefix) {
		instrumentationSkipPrefixes.add(Compiler.munge(prefix));
	}

	public static void removeInstrumentationSkipPrefix(String prefix) {
		instrumentationSkipPrefixes.remove(Compiler.munge(prefix));        
	}

	
	
	public static ArrayList<String> getInstrumentationOnlyPrefixes() {
		return instrumentationOnlyPrefixes;
	}
		
	public static void addInstrumentationOnlyPrefix(String prefix) {
		instrumentationOnlyPrefixes.add(Compiler.munge(prefix));        
	}

	public static void removeInstrumentationOnlyPrefix(String prefix) {
		instrumentationOnlyPrefixes.remove(Compiler.munge(prefix));        
	}
	
	public static boolean skipInstrumentation(String fqFnName) {        

		boolean instrument = false;
        // if any of the only is true, make instrument true
        for (String prefix : instrumentationOnlyPrefixes)
            {
            instrument |= fqFnName.startsWith(prefix);
            }

        // if any of the skips is true, make it false
        for (String prefix : instrumentationSkipPrefixes)
            {
            instrument &= !fqFnName.startsWith(prefix);
            }

        if (instrumentationSkipRegex != null)
            {
            Matcher m = instrumentationSkipRegex.matcher(fqFnName);
            instrument &= !m.find();
            }
        
		boolean skip = !getInstrumentationEnable() || !instrument;
        return skip;
	}

    //////////////////////////////
    // Instrumentation emission //
    //////////////////////////////

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

    /**
     * Emit the bytecode for a function prologue that will trace the function call and also add and return a label
     * that can be used by the function epilogue emition code to wrap the fn body on a try/catch
    */
	public static Label emitFnPrologue(GeneratorAdapter gen, ObjExpr objx, String mungedFnName, Type[] argtypes, IPersistentVector arglocals) {

		boolean skipFn = skipInstrumentation(mungedFnName);
        
		if (fnCallInstrumentationEnable && !skipFn) {

            
            Label startTry = gen.newLabel();
            gen.mark(startTry);
            
            Integer formId = (Integer) Compiler.FORM_ID.deref();
            if (formId == null) formId = 0;
			Symbol name = Symbol.create(Compiler.demunge(mungedFnName));
			String fnName = name.getName();
			String fnNs = name.getNamespace();

            // push all the args array on the stack and then            
			gen.loadArgArray(); 
            
			gen.push(fnNs); // push the function namespace
			gen.push(fnName); // push the function name
			gen.push((int)formId); // push the form-id
			gen.invokeStatic(TRACER_CLASS_TYPE, Method.getMethod("void traceFnCall(Object[], String, String, int)")); // trace the function call

            // emit binds for all function's arguments
			emitBindTraces(gen, objx, arglocals, PersistentVector.EMPTY);
            
            return startTry;
            }
        
        return null;
	}    

    /**
     * Emit the bytecode for a function epilogue that will trace the function return.
     * If a tryStartLabel is provided the epilogue will make sure that the entire function body is wrapped in a
     * try/catch block so if any exception arises during the functions body execution it will trace a stack unwind.
    */
	public static void emitFnEpilogue(GeneratorAdapter gen, String mungedFnName, IPersistentVector coord, Type retType, Label tryStartLabel) {
        
        boolean skipFn = skipInstrumentation(mungedFnName);
        
		if (fnReturnInstrumentationEnable && !skipFn) {

            Label tryEndLabel = gen.newLabel();
            Label retLabel = gen.newLabel();
            Label catchHandlerLabel = gen.newLabel();

            Integer formId = (Integer) Compiler.FORM_ID.deref();
            if (formId == null) formId = 0;
            
            // trace the return
            // push a copy of the return value
			if(Type.VOID_TYPE.equals(retType))
				{
				gen.visitInsn(Opcodes.ACONST_NULL);
				} else {
				// assumes the stack contains the value to be traced
				// duplicate the value for tracing, so we don't consume it
				dupAndBox(gen, retType);
				}
                        
			emitCoord(gen, coord); // push the coord				
            gen.push((int) formId); // push the formId
			gen.invokeStatic(TRACER_CLASS_TYPE, Method.getMethod("void traceFnReturn(Object, String, int)")); // trace the return

            // if we have a tryStartLabel it means that the fnCall was also instrumented, so we can
            // wrap a try/catch over the fn body
            if(tryStartLabel != null) {
                gen.goTo(retLabel); // jump to the return, skipping exception handling code
            
                gen.mark(tryEndLabel); // closing try block label
            
                gen.mark(catchHandlerLabel); // if anything is thrown in the fn code, handle it here
                // Throwable handler code, if we got here we have the Throwable obj on the stack
				gen.dup(); // copy the throwable ref so we can trace it
                emitCoord(gen, coord); // push the coord
                gen.push((int)formId); // push the formId
                gen.invokeStatic(TRACER_CLASS_TYPE, Method.getMethod("void traceFnUnwind(Object, String, int)")); // trace the return                
                gen.throwException(); // re-throw the throwable we have on the stack

                // setup our throwable catch handler
                gen.visitTryCatchBlock(tryStartLabel, tryEndLabel, catchHandlerLabel, "java/lang/Throwable");
            
                gen.mark(retLabel);
            }            
		}
	}

	private static void emitCoord(GeneratorAdapter gen, IPersistentVector coord) {
        HashSet<String> emittedCoords = (HashSet)Compiler.FORM_COORDS.deref();
        
		if (coord == null) {
			coord =  PersistentVector.EMPTY;
		}

		StringBuilder strCoordBuilder = new StringBuilder();
		for (int i = 0; i < coord.count(); i++) {
			strCoordBuilder.append(coord.nth(i));
			if(i < (coord.count()-1))
				strCoordBuilder.append(",");
		}
		String strCoord = strCoordBuilder.toString();
		gen.push(strCoord);
        
        emittedCoords.add(strCoord);
	}	

	public static void emitExprTrace(GeneratorAdapter gen, ObjExpr objx, IPersistentVector coord, Type retType) {
		if (exprInstrumentationEnable && coord != null) {

            Integer formId = (Integer) Compiler.FORM_ID.deref();
            if (formId == null) formId = 0;                        
                
			if ((objx instanceof FnExpr || objx instanceof NewInstanceExpr) && !skipInstrumentation(objx.name())) {
                
				// assumes the stack contains the value to be traced
				// duplicate the value for tracing, so we don't consume it
				dupAndBox(gen, retType);
								
				emitCoord(gen, coord);

				gen.push((int)formId);
		
				// trace
				gen.invokeStatic(TRACER_CLASS_TYPE,Method.getMethod("void traceExpr(Object, String, int)"));
			}		
		}
		
	}

	public static void emitBindTrace(GeneratorAdapter gen, ObjExpr objx, BindingInit bi, IPersistentVector coord) {
        if (bindInstrumentationEnable) {
            String symName = Compiler.demunge(bi.binding().name);
            Integer bIdx = bi.binding().idx;
		
            if (objx instanceof FnExpr &&
                !skipInstrumentation(((FnExpr)objx).name()) &&
                coord != null &&
                !(symName.equals("-") || symName.contains("--"))) {

                Type valType = null;
                Class primc = Compiler.maybePrimitiveType(bi.init());
                if (primc != null) valType = Type.getType(primc);

                // assume binding val on the stack
                dupAndBox(gen, valType);
                emitCoord(gen, coord);
                gen.push(symName);

                gen.invokeStatic(TRACER_CLASS_TYPE,
                    Method.getMethod("void traceBind(Object, String, String)"));

                }
        }		
	}
	 
	public static void emitBindTraces(GeneratorAdapter gen, ObjExpr objx, IPersistentVector localBindings,
			IPersistentVector coord) {

		if (bindInstrumentationEnable && objx instanceof FnExpr && !skipInstrumentation(((FnExpr) objx).name())) {
            
			for (int i = 0; i < localBindings.count(); i++) {

				LocalBinding lb = (LocalBinding) localBindings.nth(i);

				String symName = Compiler.demunge(lb.name);

				if (coord != null && !(symName.equals("-") || symName.contains("--")) && lb.used) {
					
					objx.emitLocal(gen, lb, false, null);
					
					emitCoord(gen, coord);
					gen.push(symName);

					gen.invokeStatic(TRACER_CLASS_TYPE,
						Method.getMethod("void traceBind(Object, String, String)"));
				}
			}
		}
	}

	public static void emitFormsRegistration(GeneratorAdapter gen, List<Object> forms, String sourcePath) {
        Namespace ns = (Namespace) RT.CURRENT_NS.deref();
        String fns = ns.getName().toString();

        if (!skipInstrumentation(Compiler.munge(fns))) {
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
}
