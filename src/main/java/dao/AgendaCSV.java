package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import model.Contacto;

public class AgendaCSV {
	
	final private static String FILE = "agenda.csv";
	final private static String DELETED = "00000000-0000-0000-0000-000000000000";
	
	RandomAccessFile raf = null;
	
	/**
	 * Constructor
	 */
	public AgendaCSV() {
		open();
	}
	
	/**
	 * Si el fichero no está abierto lo abre
	 */
	private void open() {
		try {
			if (raf == null) {
				raf = new RandomAccessFile(FILE, "rw");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Cierra la agenda
	 */
	public void close() {
		if (raf != null) {
			try {
				raf.close();
			} catch (IOException e) {
			} finally {
				raf = null;
			}
		}
	}

	/**
	 * Mostrar la agenda
	 * En desarrollo muestras los registros borrados
	 * En producción hay que quitar esas líneas
	 * 
	 * @return cadena con la agenda
	 */
	public String show() {
		Contacto c;
		StringBuffer sb = new StringBuffer();
		try {
			raf.seek(0L);
			while ((c = read(raf)) != null) {
				if (c.getUsuario().toString().equals(DELETED)) {
					sb.append("borrado > ");
					sb.append(c.toString());
					sb.append("\n");
				} else {
					sb.append(c.toString());
					sb.append("\n");					
				}
			}
		} catch (IOException e) {
			return "";
		}
		return sb.toString();		
	}

	/**
	 * Buscar un contacto conociendo su identificador
	 * 
	 * @param codigo
	 * @return null
	 */
	public Contacto buscarPorCodigo(String id) {
		try {
			UUID uuid = UUID.fromString(id);
			return buscarPorCodigo(uuid);
		} catch (Exception e) {			
		}
		return null;
	}

	/**
	 * Buscar un contacto conociendo su identificador
	 * 
	 * @param codigo
	 * @return null
	 */
	private Contacto buscarPorCodigo(UUID id) {
		Contacto c;
		try {
			raf.seek(0L);
			while ((c = read(raf)) != null) {
				if (c.getUsuario().equals(id)) {
					return c;
				}
			}
		} catch (IOException e) {
		}
		return null;
	}

	/**
	 * Buscar contactos conociendo los primeros caracteres de su nombre
	 * 
	 * @param inicio del nombre
	 * @return lista de contactos que cumplen con la condición o null
	 */
	public List<Contacto> buscarPorNombre(String inicio) {
		Contacto c;
		List<Contacto> contactos = new ArrayList<Contacto>();
		try {
			raf.seek(0L);
			while ((c = read(raf)) != null) {
				if (!c.getUsuario().toString().equals(DELETED) 
						&& c.getNombre().startsWith(inicio)) {
					contactos.add(c);
				}
			}
		} catch (IOException e) {
		}
		return contactos;
	}
	
	/**
	 * Añade un nuevo contacto a la agenda
	 * @param c
	 * @return true si ha sido añadido, false en caso contrario
	 */
	public boolean add(Contacto c) {
		try {
			raf.seek(raf.length());
			raf.writeBytes(c.getUsuario().toString());
			raf.writeBytes(",");
			raf.writeBytes(c.getNombre());
			raf.writeBytes(",");
			raf.writeBytes(c.getTelefono());
			raf.writeBytes(",");
			raf.writeBytes("" + c.getEdad());
			raf.writeBytes("\n");
			return true;
		} catch (IOException e) {
			return false;
		}		
	}
	
	/**
	 * Borra un contacto conociendo su identificador
	 * 
	 * @param identificador
	 * @return true si es borrado, false en caso contrario
	 */
	public boolean delete(String id) {
		try {
			UUID uuid = UUID.fromString(id);
			return delete(uuid);
		} catch (Exception e) {			
		}
		return false;
	}

	/**
	 * Borra un contacto conociendo su identificador
	 * 
	 * @param identificador
	 * @return true si es borrado, false en caso contrario
	 */
	private boolean delete(UUID codigo) {
		Contacto c;
		try {
			raf.seek(0L);
			while ((c = read(raf)) != null) {
				long posicion = raf.getFilePointer();
				if (c.getUsuario().equals(codigo)) {
					raf.seek(posicion);
					raf.writeBytes(DELETED);
					return true;
				}
			}
		} catch (IOException e) {
		}
		return false;
	}

	/**
	 * Vacía la agenda
	 */
	public void drop() {
		try {
			raf.setLength(0L);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Empaquetar la agenda
	 */
	public void pack() {
		Contacto c;
		try {
			close();
			
			// Copio el fichero original a un temporal
			Path file = Path.of(FILE);
			Path fileTmp = Path.of(FILE + ".tmp");
			Files.move(file, fileTmp, StandardCopyOption.REPLACE_EXISTING);
			
			// Recorro el temporal y escribo en original lo que no está borrado
			open();
			RandomAccessFile tmp = new RandomAccessFile(FILE + ".tmp", "rw");
			while ((c = read(tmp)) != null) {
				if (!c.getUsuario().toString().equals(DELETED)) {
					add(c);				
				}
			}
			
			tmp.close();
			Files.delete(fileTmp);
			
		} catch (IOException e) {
		}		
	}
	
	/**
	 * Leer un contacto
	 * <b>Ver en clase</b>
	 * 
	 * @param descriptor de un random access file
	 * @return contacto
	 * 
	 */
	private Contacto read(RandomAccessFile raf) {
		try {
			String campo[] = raf.readLine().split(",");
			String sUuid = campo[0];
			UUID uuid = UUID.fromString(sUuid);
			String nombre = campo[1];
			String telefono = campo[2];
			int edad = Integer.parseInt(campo[3]);
			return new Contacto(uuid, nombre, telefono, edad);
		} catch (Exception e) {
		}
		return null;
	}

}
