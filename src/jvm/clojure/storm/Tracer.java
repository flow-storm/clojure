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
	private static IFn traceExprFn = null;
	private static IFn traceBindFn = null;
	private static IFn handleExceptionFn = null;

	private static Keyword TRACE_FN_CALL_FN_KEY = Keyword.intern(null, "trace-fn-call-fn-key");
	private static Keyword TRACE_FN_RETURN_FN_KEY = Keyword.intern(null, "trace-fn-return-fn-key");
	private static Keyword TRACE_EXPR_FN_KEY = Keyword.intern(null, "trace-expr-fn-key");
	private static Keyword TRACE_BIND_FN_KEY = Keyword.intern(null, "trace-bind-fn-key");
	private static Keyword HANDLE_EXCEPTION_FN_KEY = Keyword.intern(null, "handle-exception-fn-key");
		
	static
		{				
		// For all new threads created 
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
				handleThreadException(t, e);
		});
			
		}

	public static void handleThreadException(Thread thread, Throwable ex) {
		if (handleExceptionFn != null)
			handleExceptionFn.invoke(thread, ex);
	}
	
    static public void traceFnCall(IPersistentVector fnArgs, String fnNs, String fnName, int formId) {
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

	public static void traceBind(Object val, String coord, String symName) {
		if (traceBindFn != null)
			{            
			traceBindFn.invoke(null, coord, symName, val);
			}        
	}
	
    static public void registerFormLocation(int formId, int line, String ns, String sourceFile) {
		FormRegistry.registerForm(formId, new FormLocation(formId,sourceFile, ns, line));  
	}

    static public void registerFormObject(int formId, String nsName, Object form) {
		FormRegistry.registerForm(formId, new FormObject(formId, nsName, form));
    }

	public static void setTraceFnsCallbacks(IPersistentMap callbacks) {
		traceFnCallFn = (IFn) callbacks.valAt(TRACE_FN_CALL_FN_KEY);
		traceFnReturnFn = (IFn) callbacks.valAt(TRACE_FN_RETURN_FN_KEY);
		traceExprFn = (IFn) callbacks.valAt(TRACE_EXPR_FN_KEY);
		traceBindFn = (IFn) callbacks.valAt(TRACE_BIND_FN_KEY); 
		handleExceptionFn = (IFn) callbacks.valAt(HANDLE_EXCEPTION_FN_KEY);
	}

}
