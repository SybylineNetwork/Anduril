package sybyline.satiafenris.ene;

import java.lang.reflect.*;

public interface ScriptMethod<T> {

	public Object implementation_call(Object[] args) throws Exception;

	public default T call(Object... args) throws ScriptRuntimeException {
		try {
			@SuppressWarnings("unchecked")
			T ret = (T) this.implementation_call(args);
			return ret;
		} catch(Exception e) {
			throw new ScriptRuntimeException(e);
		}
	}

	public default <Interface> Interface implement(Class<Interface> clazz) {
		@SuppressWarnings("unchecked")
		Interface ret = (Interface) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{ clazz }, (Object thiz, Method method, Object[] args) -> {
			return this.implementation_call(args);
		});
		return ret;
	}

}
