package clojure.storm;

import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.Symbol;

public class FormObject implements IForm {
	private int formId;
	private Object form;
	private String nsName;
	private String sourceFile;
	private int line;
	
	public final static Keyword EXTEND_PROTOCOL_KEY = Keyword.intern(null, "extend-protocol");
	public final static Keyword EXTEND_TYPE_KEY = Keyword.intern(null, "extend-type");
	public final static Keyword DEFMETHOD_KEY = Keyword.intern(null, "defmethod");
	public final static Keyword DEFRECORD_KEY = Keyword.intern(null, "defrecord");
	public final static Keyword DEFTYPE_KEY = Keyword.intern(null, "deftype");
	public final static Keyword DEFN_KEY = Keyword.intern(null, "defn");
	public final static Keyword UNKNOWN_KEY = Keyword.intern(null, "unknown");
	
	public FormObject(int formId, String nsName, String sourceFile, int line, Object form) {
		this.formId = formId; 
		this.form = form;
		this.nsName = nsName;
		this.sourceFile = sourceFile;
		this.line = line;
	}
	
	public Object getForm() {
		return form;
	}
	
	public Integer getId() {
		return formId;
	}
	
	public String getNs() {
		return nsName;
	}

	public String getSourceFile() {		
		return sourceFile;
	}

	public int getLine() {
		return line;
	}

	public static String multiMethodDispatchVal(ISeq form) {
		String val = null;
		if(form != null &&
			form.next() != null &&
			form.next().next() != null)
			{
			if (form.next().next().first() == null)
				val = "nil";
			else 
				val = form.next().next().first().toString();
			}            
		else		
			{
			System.out.println("Storm warning, couldn't parse multimethod dispatch value from " + form.toString());
			return "STORM-ERROR";
			}
            
			return val;
	}
	
	public static Keyword formKind(Object form) {
		if (form instanceof ISeq) {
			ISeq frm = (ISeq) form;
			if (frm.count() > 2 && (frm.first() instanceof clojure.lang.Symbol)) {
				Symbol s = (Symbol) frm.first();
				String sName = s.getName();
                
				if (sName.equals("extend-protocol"))
					return EXTEND_PROTOCOL_KEY;
				else if (sName.equals("extend-type"))
					return EXTEND_TYPE_KEY;
				else if (sName.equals("defmethod"))
					return DEFMETHOD_KEY;
				else if (sName.equals("defrecord"))
					return DEFRECORD_KEY;
				else if (sName.equals("deftype"))
					return DEFTYPE_KEY;
				else if (sName.equals("defn"))
					return DEFN_KEY;
				else
					return UNKNOWN_KEY;

			} else {
				return UNKNOWN_KEY;
			}
		} else {
			return UNKNOWN_KEY;
		}
	}
		
}
