package dao;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import model.Contacto;

public class AgendaSQLite {

	final private static String DSN = "jdbc:sqlite:db.sqlite";
	final private static String USER = "";
	final private static String PASS = "";
	final private static String DELETED = "00000000-0000-0000-0000-000000000000";

	Connection conn;
	Statement stmt = null;

	/**
	 * Constructor
	 */
	public AgendaSQLite() {
		conn = DB.getConnection();
		// open();
	}

	/**
	 * Acceso a la base de datos
	 */
	private void open() {
		try {
			conn = DriverManager.getConnection(DSN, USER, PASS);
			stmt = conn.createStatement();

			String sql = "CREATE TABLE IF NOT EXISTS agenda(UUID TEXT PRIMARY KEY, nombre TEXT NOT NULL, telefono TEXT, edad INTEGER)";
			stmt.execute(sql);
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
				stmt.close();
				conn.close();
			} catch (SQLException e) {

			} finally {
				stmt = null;
				conn = null;
			}
		}
	}

	/**
	 * Mostrar la agenda En desarrollo muestras los registros borrados En producción
	 * hay que quitar esas líneas
	 * 
	 * @return cadena con la agenda
	 */
	public String show() {
		StringBuffer sb = new StringBuffer();
		try {
			/*
			 * raf.seek(0L); while (raf.getFilePointer() < raf.length()) { String sUuid =
			 * raf.readUTF(); UUID uuid = UUID.fromString(sUuid); String nombre =
			 * raf.readUTF(); String telefono = raf.readUTF(); int edad = raf.readInt();
			 * Contacto c = new Contacto(uuid, nombre, telefono, edad); if
			 * (sUuid.equals(DELETED)) { sb.append("borrado > "); sb.append(c.toString());
			 * sb.append("\n"); } else { sb.append(c.toString()); sb.append("\n"); } }
			 */
			String query = "SELECT * FROM agenda";
			sb.append(stmt.executeQuery(query));

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
		try {
			/*
			 * raf.seek(0L); while (raf.getFilePointer() < raf.length()) { String sUuid =
			 * raf.readUTF(); UUID uuid = UUID.fromString(sUuid); String nombre =
			 * raf.readUTF(); String telefono = raf.readUTF(); int edad = raf.readInt();
			 * Contacto c = new Contacto(uuid, nombre, telefono, edad); if (uuid.equals(id))
			 * { return c; } }
			 */
			String query = "SELECT";
			ResultSet rs = stmt.executeQuery(query);
			return new Contacto(id, rs.getString(1), rs.getString(2), rs.getInt(3));
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
			/*
			 * raf.seek(0L); while (raf.getFilePointer() < raf.length()) { String sUuid =
			 * raf.readUTF(); UUID uuid = UUID.fromString(sUuid); String nombre =
			 * raf.readUTF(); String telefono = raf.readUTF(); int edad = raf.readInt();
			 * Contacto c = new Contacto(uuid, nombre, telefono, edad); if
			 * (!sUuid.equals(DELETED) && nombre.startsWith(inicio)) { contactos.add(c); } }
			 */
			String query = "SELECT * FROM agenda WHERE nombre LIKE 'a%'";
			Contacto c;
			ResultSet rs = stmt.executeQuery(query);

			// c=new Con
		} catch (SQLException e) {
		}
		return contactos;
	}

	/**
	 * Añade un nuevo contacto a la agenda
	 * 
	 * @param c
	 * @return true si ha sido añadido, false en caso contrario
	 */
	public boolean add(Contacto c) {
		try {
			/*
			 * raf.seek(raf.length()); raf.writeUTF(c.getUsuario().toString());
			 * raf.writeUTF(c.getNombre()); raf.writeUTF(c.getTelefono());
			 * raf.writeInt(c.getEdad());
			 */

			String insertion = "INSERT INTO agenda VALUES (?, ?, ?, ?)";
			PreparedStatement ps = conn.prepareStatement(insertion);
			ps.setString(1, c.getUsuario().toString());
			ps.setString(2, c.getNombre());
			ps.setString(3, c.getTelefono());
			ps.setInt(4, c.getEdad());

			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
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
			/*
			 * raf.seek(0L); while (raf.getFilePointer() < raf.length()) { long posicion =
			 * raf.getFilePointer(); String sUuid = raf.readUTF(); UUID uuid =
			 * UUID.fromString(sUuid); if (uuid.equals(codigo)) { raf.seek(posicion);
			 * raf.writeUTF(DELETED); return true; } raf.readUTF(); raf.readUTF();
			 * raf.readInt(); }
			 */
			String query = "DELETE * FROM agenda WHERE uuid = ?";

			PreparedStatement ps = conn.prepareStatement(query);

			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
		}
		return false;
	}

	/**
	 * Vacía la agenda
	 */
	public void drop() {
		try {
			// raf.setLength(0L);
			String sql = "DELETE * FROM agenda";
			stmt.executeQuery(sql);
		} catch (SQLException e) {
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
			Path file = Path.of(DSN);
			Path fileTmp = Path.of(DSN + ".tmp");
			Files.move(file, fileTmp, StandardCopyOption.REPLACE_EXISTING);

			// Recorro el temporal y escribo en original lo que no está borrado
			open();
			RandomAccessFile tmp = new RandomAccessFile(DSN + ".tmp", "rw");
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
	 * Leer un contacto <b>Ver en clase</b>
	 * 
	 * @param descriptor de un random access file
	 * @return contacto
	 */
	private Contacto read(ResultSet rs) {
		String sUuid;
		try {
			sUuid = rs.getNString("uuid");
			UUID uuid = UUID.fromString(sUuid);
			String nombre = rs.getString("nombre");
			String telefono = rs.getString("telefono");
			int edad = rs.getInt("edad");
			return new Contacto(uuid, nombre, telefono, edad);
		} catch (SQLException e) {
		}
		return null;
	}

}
