package dispositivo.interfaces;

public interface IFuncion {
	
	public String getId();
	
	public IFuncion iniciar();
	public IFuncion detener();
	
	public IFuncion encender();
	public IFuncion apagar();
	public IFuncion parpadear();
	
	public IFuncion habilitar();
	public IFuncion deshabilitar();
	public boolean estaHabilitada();

	public FuncionStatus getStatus();

}
