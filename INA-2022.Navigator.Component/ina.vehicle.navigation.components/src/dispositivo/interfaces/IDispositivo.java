package dispositivo.interfaces;

import java.util.Collection;

public interface IDispositivo {

	public String getId();
	
	public IDispositivo iniciar();
	public IDispositivo detener();
	
	public IDispositivo habilitar();
	public IDispositivo deshabilitar();
	public boolean estaHabilitado();
	
	public IDispositivo addFuncion(IFuncion f);
	public IFuncion getFuncion(String funcionId);
	public Collection<IFuncion> getFunciones();
		
}
