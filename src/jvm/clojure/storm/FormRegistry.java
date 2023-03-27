package clojure.storm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentVector;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentVector;
import clojure.lang.RT;

public class FormRegistry {
	    
	final static Keyword FORM_ID_KEY = Keyword.intern("form", "id");
	final static Keyword FORM_NS_KEY = Keyword.intern("form", "ns");
	final static Keyword FORM_FORM_KEY = Keyword.intern("form", "form");
	final static Keyword FORM_DEF_KIND_KEY = Keyword.intern("form", "def-kind");
	final static Keyword MULTIMETHOD_DISPATCH_VAL_KEY = Keyword.intern("multimethod", "dispatch-val");

	private static ConcurrentHashMap<Integer, IForm> formsTable = new ConcurrentHashMap();
	
	private static IPersistentMap makeFormMap(IForm form) {
		Object frmO = form.getForm();
		Keyword frmKind = FormObject.formKind(frmO);

		IPersistentMap ret = RT.map(FORM_ID_KEY, form.getId(),
			FORM_NS_KEY, form.getNs(),
			FORM_FORM_KEY, frmO,
			FORM_DEF_KIND_KEY, frmKind);

		if (frmKind==FormObject.DEFMETHOD_KEY) 
			ret=ret.assoc(MULTIMETHOD_DISPATCH_VAL_KEY, FormObject.multiMethodDispatchVal((ISeq)frmO));
		
		return ret;
	}                

	public static void registerForm(int formId, IForm form) {
		formsTable.put(formId, form);
	}

	public static IPersistentVector getAllForms() {
		List<IPersistentMap> forms = new ArrayList<IPersistentMap>();
		for(IForm form : formsTable.values())
			forms.add(makeFormMap(form));
			
		return PersistentVector.create(forms);
	}

	public static IPersistentMap getForm(int formId) {
		return makeFormMap(formsTable.get(formId));
	}


	
}
