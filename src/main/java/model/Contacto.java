package model;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Contacto {
	
	UUID usuario; 
	String nombre; 
	String telefono; 
	int edad;

	public Contacto(String nombre, String telefono, int edad) {
		setUsuario(UUID.randomUUID());
		setNombre(nombre);
		setTelefono(telefono);
		setEdad(edad);
	}
	
	public String toString() {
		return String.format("%s | %s | tfno: %s | %d años", usuario, nombre, telefono, edad);
		
	}
}
