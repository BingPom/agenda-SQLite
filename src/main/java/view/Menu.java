package view;

import java.util.List;

import dao.AgendaSQLite;
import io.IO;
import model.Contacto;

public class Menu {
	
	public static void main(String[] args) {
		AgendaSQLite agenda = new AgendaSQLite();
//		agenda.drop();
		
		List<String> opciones = List.of( 
				"buscar por Código", 
				"buscar por Nombre", 
				"Mostrar la agenda", 
				"Añadir un contacto", 
				"Eliminar un contacto",
				"Salir");
		
		while (true) {
			System.out.println(opciones);
			switch (IO.readString().toUpperCase().charAt(0)) {
			case 'C':
				buscarPorCodigo(agenda);
				break;
			case 'N':
				buscarPorInicioDelNombre(agenda);
				break;
			case 'M':
				mostrar(agenda);
				break;
			case 'A':
				anadirContacto(agenda);
				break;
			case 'E':
				borrarContacto(agenda);
				break;
			case 'S':
				cerrarAgenda(agenda);
				return;
			default:
			}
		}		
		
	}

	private static void cerrarAgenda(AgendaSQLite agenda) {
		agenda.close();
	}

	private static void borrarContacto(AgendaSQLite agenda) {
		IO.print("Código ? ");
		String id = IO.readString();
		boolean borrado = agenda.delete(id);
		IO.println(borrado ? "Borrado" : "No se ha podido borrar");
	}

	private static void anadirContacto(AgendaSQLite agenda) {
		IO.print("Nombre ? ");
		String nombre = IO.readString();
		IO.print("Teléfono ? ");
		String telefono = IO.readString();
		IO.print("Edad ? ");
		int edad = IO.readInt();
		boolean anadido = agenda.add(new Contacto(nombre, telefono, edad));
		IO.println(anadido ? "Añadido" : "No se ha podido añadir");
	}

	private static void mostrar(AgendaSQLite agenda) {
		System.out.println(agenda.show());
	}

	private static void buscarPorInicioDelNombre(AgendaSQLite agenda) {
		IO.print("El nombre empieza por ? ");
		String inicio = IO.readString();
		IO.println(agenda.buscarPorNombre(inicio));
	}

	private static void buscarPorCodigo(AgendaSQLite agenda) {
		IO.print("Código ? ");
		String id = IO.readString();
		IO.println(agenda.buscarPorCodigo(id));
	}

}
