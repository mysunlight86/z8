package java.io;

import org.zenframework.z8.rmi.ObjectIO;

public class ObjectOutputStream extends DataOutputStream implements ObjectOutput {

	public ObjectOutputStream(OutputStream out) throws IOException {
		super(out);
	}

	@Override
	public void writeObject(Object object) throws IOException {
		Object replacement = replaceObject(object);
		ObjectIO.write(this, replacement, object);
	}

	protected Object replaceObject(Object obj) throws IOException {
		return obj;
	}

	public void useProtocolVersion(int version) throws IOException {}

	protected boolean enableReplaceObject(boolean enable) throws SecurityException {
		return false;
	}

}
