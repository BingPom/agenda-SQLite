package dao;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import model.Contacto;

public class AgendaSQLite {
	
//	Datos de la conexión SQLite
	String dsn = "jdbc:sqlite:db.sqlite";
	String user= "";
	String pass = "";
	String sql = "";
	
	Connection conn = null;
	Statement stmt = null;
	
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
				sql = "CREATE TABLE IF NOT EXISTS agenda (uuid STRING PRIMARY KEY, nombre STRING NOT NULL, telefono STRING, edad INTEGER)";
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
	public String buscarPorCodigo(String id) {
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
	public String buscarPorNombre(String inicio) {
		StringBuffer sb = new StringBuffer();
		try {
			sql = "SELECT * FROM agenda WHERE nombre LIKE '?%'";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, inicio);
			sb.append(ps.executeQuery());
			return sb.toString();
		} catch (SQLException e) {
		}
		return "";
	}
	
	/**
	 * Añade un nuevo contacto a la agenda
	 * @param c
	 * @return true si ha sido añadido, false en caso contrario
	 */
	public boolean add(Contacto c) {
		try {
			sql = "INSERT INTO agenda VALUES (?, ?, ?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, c.getUsuario().toString());
			ps.setString(2, c.getNombre());
			ps.setString(3, c.getTelefono());
			ps.setInt(4, c.getEdad());
			ps.executeQuery();
			return true;
		} catch (SQLException e) {
		}
		return false;	
	}

	/**
	 * Borra un contacto conociendo su identificador
	 * 
	 * @param identificador
	 * @return true si es borrado, false en caso contrario
	 */
	public boolean delete(String codigo) {
		try {
			sql = "DELETE FROM agenda WHERE uuid = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, codigo);
			ps.executeQuery();
		}catch (SQLException e) {
		}
		return false;
	}

	/**
	 * Vacía la agenda
	 */
	public void drop() {
		try {
//			La borramos
			sql = "DROP TABLE IF EXISTS agenda";
			stmt.executeUpdate(sql);
			
//			La volvemos a crear
			sql = "CREATE TABLE IF NOT EXISTS agenda (uuid STRING PRIMARY KEY, nombre STRING NOT NULL, telefono STRING, edad INTEGER)";
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
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
