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
		
	private static ConcurrentHashMap<IPersistentVector,IPersistentVector> coordCache = new ConcurrentHashMap();

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

	static public void traceExpr(Object val, Integer[] coord, int formId) {        
        if (traceExprFn != null)
			{
			IPersistentVector c = internCoord(coord);
			traceExprFn.invoke(null, val, c, formId);
			}        
    }

    static public void traceFnReturn(Object retVal, Integer[] coord, int formId) {        
        if (traceFnReturnFn != null)
			{
			IPersistentVector c = internCoord(coord);
			traceFnReturnFn.invoke(null, retVal, c, formId);
			}        
    }

	public static void traceBind(Object val, Integer[] coord, String symName) {
		if (traceBindFn != null)
			{
			IPersistentVector c = internCoord(coord);
			traceBindFn.invoke(null, c, symName, val);
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
		
	private static IPersistentVector internCoord(Integer... coord) {
		IPersistentVector k = PersistentVector.adopt(coord);
		if(coordCache.containsKey(k)) {
			return coordCache.get(k);
			} else {            
			coordCache.put(k, k);
			return k;
			}	
	}

}
