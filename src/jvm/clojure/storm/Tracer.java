package clojure.storm;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.PersistentVector;

public class Tracer {
	
	private static IFn traceFnCallFn = null;
	private static IFn traceFnReturnFn = null;
	private static IFn traceFnUnwindFn = null;
	private static IFn traceExprFn = null;
	private static IFn traceBindFn = null;
    
    // TODO: this are depracated, remove when it is safe
    private static Keyword TRACE_FN_CALL_FN_KEY = Keyword.intern(null, "trace-fn-call-fn-key");
	private static Keyword TRACE_FN_RETURN_FN_KEY = Keyword.intern(null, "trace-fn-return-fn-key");
	private static Keyword TRACE_EXPR_FN_KEY = Keyword.intern(null, "trace-expr-fn-key");
	private static Keyword TRACE_BIND_FN_KEY = Keyword.intern(null, "trace-bind-fn-key");
    
    private static Keyword TRACE_FN_CALL_FN = Keyword.intern(null, "trace-fn-call-fn");
	private static Keyword TRACE_FN_RETURN_FN = Keyword.intern(null, "trace-fn-return-fn");
	private static Keyword TRACE_FN_UNWIND_FN = Keyword.intern(null, "trace-fn-unwind-fn");
	private static Keyword TRACE_EXPR_FN = Keyword.intern(null, "trace-expr-fn");
	private static Keyword TRACE_BIND_FN = Keyword.intern(null, "trace-bind-fn");
    
    static public void traceFnCall(Object[] fnArgs, String fnNs, String fnName, int formId) {
		if (traceFnCallFn != null)
			traceFnCallFn.invoke(null, fnNs, fnName, fnArgs, formId);
	}

	static public void traceExpr(Object val, String coord, int formId) {        
        if (traceExprFn != null)
			{
			traceExprFn.invoke(null, val, coord, formId);
			}        
    }

    static public void traceFnReturn(Object retVal, String coord, int formId) {        
        if (traceFnReturnFn != null)
			{            
			traceFnReturnFn.invoke(null, retVal, coord, formId);
			}        
    }

    static public void traceFnUnwind(Object throwable, String coord, int formId) {        
        if (traceFnUnwindFn != null)
			{            
			traceFnUnwindFn.invoke(null, throwable, coord, formId);
			}        
    }

	public static void traceBind(Object val, String coord, String symName) {
		if (traceBindFn != null)
			{            
			traceBindFn.invoke(null, coord, symName, val);
			}        
	}
	
    static public void registerFormLocation(int formId, int line, String ns, String sourceFile) {
		FormRegistry.registerForm(formId, new FormLocation(formId, sourceFile, ns, line));
	}

    static public void registerFormObject(int formId, String nsName, String sourceFile, int line, Object form) {
		FormRegistry.registerForm(formId, new FormObject(formId, nsName, sourceFile, line, form));
    }

	public static void setTraceFnsCallbacks(IPersistentMap callbacks) {
        // We need to support two sets of keys for some time because I messed up the key names
        // and changing them will make some versions of FlowStorm that relays on the old keys to stop working
        
        // Deprecated keys
        if (callbacks.valAt(TRACE_FN_CALL_FN_KEY) != null)
            traceFnCallFn = (IFn) callbacks.valAt(TRACE_FN_CALL_FN_KEY);

        if (callbacks.valAt(TRACE_FN_RETURN_FN_KEY) != null)
            traceFnReturnFn = (IFn) callbacks.valAt(TRACE_FN_RETURN_FN_KEY);

        if (callbacks.valAt(TRACE_EXPR_FN_KEY) != null)
            traceExprFn = (IFn) callbacks.valAt(TRACE_EXPR_FN_KEY);

        if (callbacks.valAt(TRACE_BIND_FN_KEY) != null)
            traceBindFn = (IFn) callbacks.valAt(TRACE_BIND_FN_KEY);
        
        // New keys
        
        if (callbacks.valAt(TRACE_FN_CALL_FN) != null)
            traceFnCallFn = (IFn) callbacks.valAt(TRACE_FN_CALL_FN);

        if (callbacks.valAt(TRACE_FN_RETURN_FN) != null)
            traceFnReturnFn = (IFn) callbacks.valAt(TRACE_FN_RETURN_FN);

        if (callbacks.valAt(TRACE_FN_UNWIND_FN) != null)
            traceFnUnwindFn = (IFn) callbacks.valAt(TRACE_FN_UNWIND_FN);

        if (callbacks.valAt(TRACE_EXPR_FN) != null)
            traceExprFn = (IFn) callbacks.valAt(TRACE_EXPR_FN);

        if (callbacks.valAt(TRACE_BIND_FN) != null)
            traceBindFn = (IFn) callbacks.valAt(TRACE_BIND_FN);
        
	}

}
