package dao;

import java.sql.Statement;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import model.Contacto;

public class AgendaSQLite {
	
//	Datos de la conexión SQLite
	String dsn = "jdbc:sqlite:db.sqlite";
	String user= "";
	String pass = "";
	String sql = "";
	
	Connection conn = null;
	Statement stmt = null;
	
	final private static String FILE = "agenda.dat";
	final private static String DELETED = "00000000-0000-0000-0000-000000000000";
	
	RandomAccessFile raf = null;
	
	/**
	 * Constructor
	 */
	public AgendaSQLite() {
		open();
	}
	
	/**
	 * Si el fichero no está abierto lo abre
	 */
	private void open() {
		try {
			if (conn == null) {
				conn = DriverManager.getConnection(dsn, user, pass);
				sql = "create TABLE IF not EXISTS agenda (uuid STRING PRIMARY KEY, nombre STRING NOT NULL, telefono STRING, edad INTEGER)";
				stmt = conn.createStatement();
				stmt.executeUpdate(sql);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Cierra la agenda
	 */
	public void close() {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				conn = null;
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
		StringBuffer sb = new StringBuffer();
		try {
			sql = "SELECT * FROM agenda";
			sb.append(stmt.executeUpdate(sql));
		} catch (SQLException e) {
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
	public String buscarPorCodigo(UUID id) {
		StringBuffer sb = new StringBuffer();
		try {
			sql = "SELECT * FROM agenda WHERE uuid = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, id.toString());
			sb.append(ps.executeQuery());
			return sb.toString();
		} catch (SQLException e) {
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
		List<Contacto> contactos = new ArrayList<Contacto>();
		try {
			raf.seek(0L);
			while (raf.getFilePointer() < raf.length()) {
				String sUuid = raf.readUTF();
				UUID uuid = UUID.fromString(sUuid);
				String nombre = raf.readUTF();
				String telefono = raf.readUTF();
				int edad = raf.readInt();
				Contacto c = new Contacto(uuid, nombre, telefono, edad);
				if (!sUuid.equals(DELETED) && nombre.startsWith(inicio)) {
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
			raf.writeUTF(c.getUsuario().toString());
			raf.writeUTF(c.getNombre());
			raf.writeUTF(c.getTelefono());
			raf.writeInt(c.getEdad());
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
		try {
			raf.seek(0L);
			while (raf.getFilePointer() < raf.length()) {
				long posicion = raf.getFilePointer();
				String sUuid = raf.readUTF();
				UUID uuid = UUID.fromString(sUuid);
				if (uuid.equals(codigo)) {
					raf.seek(posicion);
					raf.writeUTF(DELETED);
					return true;
				}
				raf.readUTF();
				raf.readUTF();
				raf.readInt();
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
		try {
			close();
			
			// Copio el fichero original a un temporal
			Path file = Path.of(FILE);
			Path fileTmp = Path.of(FILE + ".tmp");
			Files.move(file, fileTmp, StandardCopyOption.REPLACE_EXISTING);
			
			// Recorro el temporal y escribo en original lo que no está borrado
			open();
			RandomAccessFile tmp = new RandomAccessFile(FILE + ".tmp", "rw");
			while (tmp.getFilePointer() < tmp.length()) {
				String sUuid = tmp.readUTF();
				UUID uuid = UUID.fromString(sUuid);
				String nombre = tmp.readUTF();
				String telefono = tmp.readUTF();
				int edad = tmp.readInt();
				Contacto c = new Contacto(uuid, nombre, telefono, edad);
				if (!sUuid.equals(DELETED)) {
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
	private Contacto read(RandomAccessFile raf) {
		String sUuid;
		try {
			sUuid = raf.readUTF();
			UUID uuid = UUID.fromString(sUuid);
			String nombre = raf.readUTF();
			String telefono = raf.readUTF();
			int edad = raf.readInt();
			return new Contacto(uuid, nombre, telefono, edad);		
		} catch (IOException e) {
		}
		return null;
	}
	 */

}
