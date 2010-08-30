package pl.psnc.dl.ege.validator.xml;

import pl.psnc.dl.ege.ExceptionListener;

class ExceptionListenerImpl implements ExceptionListener
{
	private Exception exception = null;
	
	public void catchException(Exception ex)
	{
		exception = ex;
	}
	
	public Exception throwException(){
		return exception;
	}
	
}
