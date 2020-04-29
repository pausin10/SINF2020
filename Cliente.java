import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class Cliente {
	static final String IDADMIN = "admin";
	static final String PASS = "admin";

	public static void main(String[] args) {
		try {
			Scanner scan = new Scanner(System.in);
			String link = "jdbc:mysql://localhost/";
			String user = "alvaro";
			String password = "alvaro9961";
			String baseDatos = "GrupoC?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&useSSL=false";
			Class.forName("com.mysql.jdbc.Driver");
			System.out.println("Base de datos conectada.");
			Connection conexion = null;
			Statement state = null;
			conexion = DriverManager.getConnection(link + baseDatos, user, password);
			state = conexion.createStatement();
			while (true) {
				menu(scan, state);
			}
		} catch (SQLException e) {
			System.out.println("Que ha pachado");
			e.printStackTrace();
		} catch (Exception e) {

		}

	}

	private static void menu(Scanner scan, Statement state) throws SQLException {
		String sql = null;
		System.out.println("Escoja que acción desea realizar:");
		System.out.println(" 1.  Añadir un cliente al sistema");
		System.out.println(" 2.  Consultar los datos de un cliente");
		System.out.println(" 3.  Cambiar el estado de un Evento");
		System.out.println(" 4.  Mostrar eventos disponibles");
		System.out.println(" 5.  Filtrar Eventos");
		System.out.println(" 6.  Crear un nuevo Evento");
		System.out.println(" 7.  Eliminar Evento");
		System.out.println(" 8.  Eliminar Espectáculo");
		System.out.println(" 9.  Consultar precio de una localidad");
		System.out.println(" 10. Consultar localidades disponibles de una Grada en un Evento");
		System.out.println(" 11. Comprar entrada");
		System.out.println(" 12. Pre reservar una entrada.");
		System.out.println(" 13. Anular una compra");
		System.out.println(" 14. Salir ");
		System.out.println();
		String respuesta = scan.nextLine();
		switch (Integer.valueOf(respuesta)) {
		case 1:
			System.out.println("Ha seleccionado registrar un nuevo cliente.\n");
			addCliente(scan, state, sql);
			break;
		case 2:
			System.out.println("Ha seleccionado consultar sus datos personales.\n");
			buscarCliente(scan, state, sql);
			break;
		case 3:
			System.out.println("Has seleccionado cambiar el estado de un Evento\n");
			if (comprobarPermisos(scan)) {
				cambiarEstado(scan, state, sql);
			}
			break;
		case 4:
			System.out.println("Has seleccionado ver la lista de eventos disponibles\n");
			listarEspectaculos(state, sql);
			break;
		case 5:
			System.out.println("Has seleccionado filtrar Espectaculos\n");
			filtarEspectaculos(state, sql, scan);
			break;
		case 6:
			System.out.println("Has seleccionado crear un nuevo Evento.\n");
			if (comprobarPermisos(scan)) {
				addEvento(scan, state, sql);
			}
			break;
		case 7:
			System.out.println("Has seleccionado eliminar un Evento\n");
			if (comprobarPermisos(scan)) {
				deleteEvento(scan, state, sql);
			}
			break;
		case 8:
			System.out.println("Has seleccionado eliminar un Espectáculo\n");
			if (comprobarPermisos(scan)) {
				deleteEspectaculo(scan, state, sql);
			}
			break;
		case 9:
			System.out.println("Has seleccionado consultar el precio de una Localidad\n");
			consultarPrecio(scan, state, sql);

			break;
		case 10:
			System.out.println("Has seleleccionado consultar las localidades de una Grada en un Evento\n");
			consultarLocalidad(scan, state, sql);

			break;
		case 11:
			System.out.println("Ha seleccionado comprar entrada.\n");
			comprarEntrada(scan, state, sql);
			break;
		case 12:
			System.out.println("Has seleccionado pre reservar una entrada");
			prereservarEntrada(scan, state, sql);
			break;
		case 13:
			System.out.println("Ha solicitado anular la compra de una/s Localidad\n");
			anularLocalidad(scan, state, sql);
			break;
		case 14:
			System.out.println("Has seleccionado salir.");
			System.exit(0);
		default:
			System.out.println("La opción seleccionada no es válida.");
			System.exit(0);
		}

	}

	private static void prereservarEntrada(Scanner scan, Statement state, String sql) {
		try {

			System.out.println("¿Qué evento desea consultar la disponibilidad de localidades?\n");
			String consulta = "SELECT ev.idEvento,e.titulo from Espectaculo e\n"
					+ "INNER join Evento ev on ev.idEspectaculo = e.idEspectaculo\n" + "where estado='abierto'";
			String id = null;
			String nombreRecinto = null;
			String idRecinto = null;
			String idGrada = null;
			try {
				ResultSet answer = state.executeQuery(consulta);

				while (answer.next()) {
					System.out.println(
							"id: " + answer.getString("idEvento") + "   Evento: " + answer.getString("titulo") + "\n");
				}
				System.out.println("Introduzca el id:");
				id = scan.nextLine();
				sql = "SELECT DISTINCT r.Nombre FROM Evento_Grada e \n"
						+ "INNER JOIN Recinto r ON r.idRecinto=e.idRecinto\n" + "WHERE e.idEvento='" + id + "'";
				answer = state.executeQuery(sql);
				while (answer.next()) {
					nombreRecinto = answer.getString("Nombre");
					System.out.println(
							"Has seleccionado el evento " + id + " que se celebra en: " + answer.getString("Nombre"));
				}
				sql = "select idRecinto from Recinto where Nombre='" + nombreRecinto + "'";
				answer = state.executeQuery(sql);
				if (answer.next()) {
					idRecinto = answer.getString("idRecinto");
				}
				sql = "select idGrada,Nombre from Grada where idRecinto='" + idRecinto + "'";
				System.out.println("\nIndique en cuál de las siguientes gradas desea consultar\n");
				answer = state.executeQuery(sql);
				while (answer.next()) {
					System.out
							.println("Grada:" + answer.getString("idGrada") + " Nombre: " + answer.getString("Nombre"));
				}
				System.out.println("\nIntroduzca el id de la grada:\n");
				idGrada = scan.nextLine();
				sql = "select idLocalidad from Localidad where idRecinto='" + idRecinto + "' and idGrada='" + idGrada
						+ "'";
				answer = state.executeQuery(sql);
				while (answer.next()) {
					System.out.println("ID Localidad: " + answer.getString("idLocalidad"));
				}
			} catch (SQLException e) {

			}

			System.out.println("Introduzca el id o ids de las localidades que desea pre reservar: ");
			String idLocalidad = scan.nextLine();
			System.out.println("Introduzca su dni:");
			String dni = scan.nextLine();
			System.out.println("Introduzca el tipo de usuario para el que pre reserva la entrada:");
			String tipo = scan.nextLine();
			String idLocalidadAux[] = idLocalidad.split(" ");
			for (int i = 0; i < idLocalidadAux.length; i++) {
				String query = "call reservar(" + Integer.valueOf(id) + "," + Integer.valueOf(idGrada) + ","
						+ Integer.valueOf(idLocalidadAux[i]) + ",'" + tipo + "','" + dni + "',@idReserva);";
				ResultSet rs = state.executeQuery(query);

				query = "SELECT @idReserva;";
				rs = state.executeQuery(query);
				String idError = null;
				while (rs.next()) {
					idError = rs.getString("@idReserva");
					if (Integer.valueOf(idError) > 0) {
						System.out.println("Resultado de la compra " + (i + 1) + ": " + idError);
					} else {
						query = "select Descripcion from Error where idError=" + Integer.valueOf(idError);
						rs = state.executeQuery(query);
						while (rs.next()) {
							System.out.println(rs.getString("Descripcion") + "\n");
						}
					}
				}
				
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private static void listarEspectaculos(Statement state, String sql) {
		System.out.println("Esta es la lista de eventos disponibles:\n");
		sql = "SELECT e.titulo, ev.fecha, ev.hora from Espectaculo e\n"
				+ "INNER join Evento ev on ev.idEspectaculo = e.idEspectaculo\n" + "where estado='abierto'";
		try {
			ResultSet answer = state.executeQuery(sql);

			while (answer.next()) {
				System.out.println("Evento: " + answer.getString("Titulo") + "   Fecha: " + answer.getString("fecha")
						+ "  Hora: " + answer.getString("hora") + "\n");
			}
		} catch (SQLException e) {

			e.printStackTrace();
		}

	}

	private static String addEspectaculo(Scanner scan, Statement state, String sql) {
		System.out.println("Introduzca de que tipo será:");
		String tipo = scan.nextLine();
		System.out.println("Introduzca que se celebrará:");
		String titulo = scan.nextLine();
		System.out.println("Introduzca una descripción (opcional):");
		String descripcion = scan.nextLine();
		sql = "insert into Espectaculo values(" + null + ",'" + tipo + "','" + titulo + "','" + descripcion + "')";
		try {
			state.executeUpdate(sql);
			return titulo;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private static void anularLocalidad(Scanner scan, Statement state, String sql) {
		System.out.println("¿De que evento desea anular su entrada?\n");
		String consulta = "SELECT ev.idEvento,e.titulo from Espectaculo e\n"
				+ "INNER join Evento ev on ev.idEspectaculo = e.idEspectaculo\n" + "where estado='abierto'";
		String id = null;
		String nombreRecinto = null;
		String idRecinto = null;
		String idGrada = null;
		String idLocalidad = null;
		try {
			ResultSet answer = state.executeQuery(consulta);
			while (answer.next()) {
				System.out.println(
						"id: " + answer.getString("idEvento") + "   Evento: " + answer.getString("Titulo") + "\n");
			}
			System.out.println("Introduzca el id:");
			id = scan.nextLine();
			sql = "SELECT DISTINCT r.Nombre FROM Evento_Grada e \n"
					+ "INNER JOIN Recinto r ON r.idRecinto=e.idRecinto\n" + "WHERE e.idEvento='" + id + "'";
			answer = state.executeQuery(sql);
			while (answer.next()) {
				nombreRecinto = answer.getString("Nombre");
				System.out.println(
						"Has seleccionado el evento " + id + " que se celebra en: " + answer.getString("Nombre"));
			}
			sql = "select idRecinto from Recinto where Nombre='" + nombreRecinto + "'";
			answer = state.executeQuery(sql);
			if (answer.next()) {
				idRecinto = answer.getString("idRecinto");
			}
			sql = "select idGrada,Nombre from Grada where idRecinto='" + idRecinto + "'";
			System.out.println("\nIndique en cuál de las siguientes gradas tiene la entrada para anular\n");
			answer = state.executeQuery(sql);
			while (answer.next()) {
				System.out.println("Grada:" + answer.getString("idGrada") + " Nombre: " + answer.getString("Nombre"));
			}
			System.out.println("\nIntroduzca el id de la grada:\n");
			idGrada = scan.nextLine();
			sql = "select idLocalidad from Localidad where idRecinto='" + idRecinto + "' and idGrada='" + idGrada + "'";
			System.out.println("\nIndique cual es la Localidad que desea anular\n");
			answer = state.executeQuery(sql);
			while (answer.next()) {
				System.out.println("ID Localidad: " + answer.getString("idLocalidad"));
			}
			System.out.println("\nIntroduzca el id de la localidad:\n");
			idLocalidad = scan.nextLine();

			System.out.println("Introduzca su dni:");
			String dni = scan.nextLine();

			String query = "call anular(" + Integer.valueOf(id) + "," + Integer.valueOf(idGrada) + ","
					+ Integer.valueOf(idLocalidad) + ",'" + dni + "',@Penalizacion);";
			answer = state.executeQuery(query);
			query = "SELECT @Penalizacion;";
			answer = state.executeQuery(query);
			String idError = null;

			while (answer.next()) {
				idError = answer.getString("@Penalizacion");
				if (Integer.valueOf(idError) >= 0) {
					System.out.println("Se ha anulado correctamente con una penalizacion de " + idError);
				} else {
					query = "select Descripcion from Error where idError=" + Integer.valueOf(idError);
					answer = state.executeQuery(query);
					while (answer.next()) {
						System.out.println(answer.getString("Descripcion") + "\n");
					}
					
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private static void consultarLocalidad(Scanner scan, Statement state, String sql) {
		System.out.println("¿Qué evento desea consultar la disponibilidad de localidades?\n");
		String consulta = "SELECT ev.idEvento,e.titulo from Espectaculo e\n"
				+ "INNER join Evento ev on ev.idEspectaculo = e.idEspectaculo\n" + "where estado='abierto'";
		String id = null;
		String nombreRecinto = null;
		String idRecinto = null;
		try {
			ResultSet answer = state.executeQuery(consulta);

			while (answer.next()) {
				System.out.println(
						"id: " + answer.getString("idEvento") + "   Evento: " + answer.getString("titulo") + "\n");
			}
			System.out.println("Introduzca el id:");
			id = scan.nextLine();
			sql = "SELECT DISTINCT r.Nombre FROM Evento_Grada e \n"
					+ "INNER JOIN Recinto r ON r.idRecinto=e.idRecinto\n" + "WHERE e.idEvento='" + id + "'";
			answer = state.executeQuery(sql);
			while (answer.next()) {
				nombreRecinto = answer.getString("Nombre");
				System.out.println(
						"Has seleccionado el evento " + id + " que se celebra en: " + answer.getString("Nombre"));
			}
			sql = "select idRecinto from Recinto where Nombre='" + nombreRecinto + "'";
			answer = state.executeQuery(sql);
			if (answer.next()) {
				idRecinto = answer.getString("idRecinto");
			}
			sql = "select idGrada,Nombre from Grada where idRecinto='" + idRecinto + "'";
			System.out.println("\nIndique en cuál de las siguientes gradas desea consultar\n");
			answer = state.executeQuery(sql);
			while (answer.next()) {
				System.out.println("Grada:" + answer.getString("idGrada") + " Nombre: " + answer.getString("Nombre"));
			}
			System.out.println("\nIntroduzca el id de la grada:\n");
			String idGrada = scan.nextLine();
			sql = "select idLocalidad from Localidad where idRecinto='" + idRecinto + "' and idGrada='" + idGrada
					+ "' and Estado='Libre'";
			answer = state.executeQuery(sql);
			while (answer.next()) {
				System.out.println("ID Localidad: " + answer.getString("idLocalidad"));
			}
		} catch (SQLException e) {
	
		}

	}

	private static void consultarPrecio(Scanner scan, Statement state, String sql) {
		System.out.println("Escoja el evento de los siguientes del que quiere consultar los precios.\n");
		String consulta = "SELECT ev.idEvento,e.titulo from Espectaculo e\n"
				+ "INNER join Evento ev on ev.idEspectaculo = e.idEspectaculo\n" + "where estado='abierto'";
		String id = null;
		try {
			ResultSet answer = state.executeQuery(consulta);

			while (answer.next()) {
				System.out.println(
						"id: " + answer.getString("idEvento") + "   Evento: " + answer.getString("titulo") + "\n");
			}
			System.out.println("Introduzca el id:");
			id = scan.nextLine();
		} catch (SQLException e) {

		}

		sql = "SELECT g.Nombre,e.usuario,e.precio FROM Evento_Grada e\n"
				+ "INNER JOIN Grada g on g.idGrada=e.idGrada \n" + "and g.idRecinto=e.idRecinto\n"
				+ "WHERE e.idEvento='" + id + "'";
		try {
			ResultSet answer = state.executeQuery(sql);

			while (answer.next()) {
				System.out.println("NombreGrada: " + answer.getString("Nombre") + "   TipoUsuario: "
						+ answer.getString("usuario") + "   Precio: " + answer.getString("precio") + "\n");
			}
		} catch (SQLException e) {

		}

	}

	private static void cambiarEstado(Scanner scan, Statement state, String sql) {
		System.out.println("Estos son los eventos y su estado actual:\n");
		sql = " select Evento.idEvento,Espectaculo.titulo,Evento.fecha,Evento.hora,Evento.Estado from Espectaculo \n"
				+ "     inner join Evento on Evento.idEspectaculo=Espectaculo.idEspectaculo where Evento.Estado != 'Finalizado' ";

		try {
			ResultSet answer = state.executeQuery(sql);
			while (answer.next()) {
				System.out.println("ID: " + answer.getString("idEvento") + " EVENTO: " + answer.getString("titulo")
						+ " FECHA: " + answer.getString("fecha") + "  HORA: " + answer.getString("hora") + " ESTADO: "
						+ answer.getString("Estado"));
			}
			System.out.println("Introduzca el id del Evento que desea cambiar de estado: ");
			String id = scan.nextLine();
			sql = "select Estado from Evento where idEvento='" + id + "'";
			answer = state.executeQuery(sql);
			if (answer.next()) {
				String Estado = answer.getString("Estado");
				if (Estado.equals("Abierto")) {
					sql = "update Evento set Estado='Cerrado' where idEvento='" + id + "'";
				} else {
					sql = "update Evento set Estado='Abierto' where idEvento='" + id + "'";
				}
				state.executeUpdate(sql);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void deleteEspectaculo(Scanner scan, Statement state, String sql) {
		System.out.println("Lista de Espectáculos disponibles\n");
		sql = "select idEspectaculo,titulo from Espectaculo";
		try {
			ResultSet answer = state.executeQuery(sql);
			while (answer.next()) {
				System.out.println(
						"ID: " + answer.getString("idEspectaculo") + "  ESPECTÁCULO: " + answer.getString("titulo"));
			}
			System.out.println("Introduzca el id del espectáculo que desea eliminar:");
			String id = scan.nextLine();
			sql = "delete from Espectaculo_Participante where idEspectaculo='" + id + "'";
			state.executeUpdate(sql);
			sql = "select idEvento from Evento where idEspectaculo='" + id + "'";
			answer = state.executeQuery(sql);
			String idEvento = null;
			String idAux = "";
			while (answer.next()) {
				idEvento = answer.getString("idEvento");
				idAux += idEvento + " ";
			}
			String[] idSplit = idAux.split(" ");
			for (int j = 0; j < idSplit.length; j++) {
				sql = "delete from Evento_Grada where idEvento='" + idSplit[j] + "'";
				state.executeUpdate(sql);
			}
			sql = "delete from Evento where idEspectaculo='" + id + "'";
			state.executeUpdate(sql);
			sql = "delete from Espectaculo where idEspectaculo='" + id + "'";
			state.executeUpdate(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void deleteEvento(Scanner scan, Statement state, String sql) {
		sql = " select Evento.idEvento,Espectaculo.titulo,Evento.fecha,Evento.hora from Espectaculo \n"
				+ "     inner join Evento on Evento.idEspectaculo=Espectaculo.idEspectaculo where Evento.Estado = 'Abierto' ";
		try {
			System.out.println("Lista de Eventos disponibles\n");
			ResultSet answer = state.executeQuery(sql);
			while (answer.next()) {
				System.out.println("ID: " + answer.getString("idEvento") + " EVENTO: " + answer.getString("titulo")
						+ " FECHA: " + answer.getString("fecha") + "  HORA: " + answer.getString("hora"));

			}
			System.out.println("Introduzca el id del evento de que desea eliminar: ");
			String id = scan.nextLine();
			sql = "Delete from Evento_Grada where idEvento='" + id + "'";
			state.executeUpdate(sql);
			sql = "Delete from Evento where idEvento='" + id + "'";
			state.executeUpdate(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void addEvento(Scanner scan, Statement state, String sql) {
		String titulo = addEspectaculo(scan, state, sql);
		String idEspectaculo = null;
		sql = "select idEspectaculo from Espectaculo where Titulo='" + titulo + "'";
		try {
			ResultSet answer = state.executeQuery(sql);
			while (answer.next()) {
				idEspectaculo = answer.getString("idEspectaculo");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("En que Recinto tendrá lugar:");
		String recinto = scan.nextLine();
		System.out.println("Cuál será el aforo máximo:");
		String aforo = scan.nextLine();
		String idRecinto = null;
		sql = "select idRecinto from Recinto where Nombre='" + recinto + "'";
		try {
			ResultSet answer = state.executeQuery(sql);
			while (answer.next()) {
				idRecinto = answer.getString("idRecinto");
			}
			if (idRecinto == null) {
				sql = "insert into Recinto values (" + null + ",'" + recinto + "','" + Integer.valueOf(aforo) + "')";
				state.executeUpdate(sql);
			}
		} catch (SQLException e) {

		}
		System.out.println("Qué día se celebrará:(yyyy:mm:dd)");
		String fecha = scan.nextLine();
		System.out.println("A qué hora:(hh:mm:ss)");
		String hora = scan.nextLine();
		System.out.println("Duración máxima de una pre reserva:");
		String t1 = scan.nextLine();
		System.out.println("Minutos hasta el inicio del evento donde está permitida la pre reserva:");
		String t2 = scan.nextLine();
		System.out.println("A falta de cuántos minutos las anulaciones tendrán recarga y su valor: ");
		String t3Penaliza = scan.nextLine();
		String aux[] = t3Penaliza.split(" ");
		String t3 = aux[0];
		String penalizacion = aux[1];
		sql = "insert into Evento values(" + null + ",'" + idEspectaculo + "','Abierto','" + fecha + "','" + hora
				+ "','" + Integer.valueOf(penalizacion) + "','" + Integer.valueOf(t1) + "','" + Integer.valueOf(t2)
				+ "','" + Integer.valueOf(t3) + "')";
		try {
			state.executeUpdate(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void filtarEspectaculos(Statement state, String sql, Scanner scan) throws SQLException {

		System.out.println("Seleccione el criterio por el cual quiere filtrar: \n");
		System.out.println("1. Participante ");
		System.out.println("2. Titulo del espectáculo");
		System.out.println("3. Tipo del espectáculo");
		System.out.println("4. Fecha");
		System.out.println("5. Recinto");
		String eleccion = scan.nextLine();
		switch (Integer.valueOf(eleccion)) {
		case 1:
			System.out.println("Introduzca el Participante:");
			String filtrado = scan.nextLine();
			System.out.println("\n\n--Filtrando la búsqueda por PARTICIPANTE=" + filtrado);
			sql = "CALL filtrar('Participante', '" + filtrado.replace(" ", "-") + "');";
			ResultSet proc = state.executeQuery(sql);
			busqueda(state, sql);
			break;
		case 2:
			System.out.println("Introduzca el titulo del espectáculo:");
			filtrado = scan.nextLine();
			System.out.println("\n\n--Filtrado por TITULO DEL ESPECTACULO:" + filtrado);
			sql = "CALL filtrar('Titulo', '" + filtrado.replace(" ", "-") + "');";
			proc = state.executeQuery(sql);
			busqueda(state, sql);
			break;
		case 3:

			System.out.println("Introduzca el tipo de Espectaculo:");
			filtrado = scan.nextLine();
			System.out.println("\n\n--Filtrado por TIPO DE  ESPECTACULO:" + filtrado);
			sql = "CALL filtrar('Tipo', '" + filtrado + "');";
			proc = state.executeQuery(sql);
			busqueda(state, sql);
			break;
		case 4:
			System.out.println("Introduzca la fecha (yyyy-mm-dd):");
			filtrado = scan.nextLine();
			System.out.println("\n\n--Filtrado por FECHA DEL EVENTO:" + filtrado);
			sql = "CALL filtrar('Fecha', '" + filtrado + "');";
			proc = state.executeQuery(sql);
			busqueda(state, sql);
			break;
		case 5:
			System.out.println("Introduzca el Recinto:");
			filtrado = scan.nextLine();
			System.out.println("\n\n--Filtrado por RECINTO:" + filtrado);
			sql = "CALL filtrar('Recinto', '" + filtrado.replace(" ", "-") + "');";
			proc = state.executeQuery(sql);
			busqueda(state, sql);
			break;
		default:
			break;
		}

	}

	private static void busqueda(Statement state, String sql) {

		String idEspectaculo = "";

		System.out.println(
				"-----------------------------------------------------------------------------------------------------");

		try {

			// Tenemos que comprobar si está vacio,si no ha habido resultados

			sql = "SELECT * FROM Espectaculo natural join (select * from Evento natural join Resultado)a;";

			ResultSet evento = state.executeQuery(sql);

			// Ahora consultamos los datos de ese Evento

			while (evento.next()) {

				System.out.println("Id del Evento: " + evento.getString("idEvento")

						+ "\nFecha: " + evento.getString("Fecha")

						+ "\nHora: " + evento.getString("Hora")

						+ "\nTitulo del Espectáculo: " + evento.getString("Titulo")

						+ "\nTipo de Espectáculo: " + evento.getString("Tipo")

						+ "\nDescripcion: " + evento.getString("Descripcion") + "\n");

				System.out.println(
						"-----------------------------------------------------------------------------------------------------");

			}

		} catch (SQLException e) {

			e.printStackTrace();

		}

	}

	private static void buscarCliente(Scanner scan, Statement state, String sql) {

		System.out.println("Introduzca su identificador único(DNI)");
		String id = "'" + scan.nextLine() + "'";
		sql = "select * from Cliente where DNI=" + id;
		try {
			ResultSet answer = state.executeQuery(sql);
			if (!(answer.next())) {
				System.out.println("No existe ningún cliente con identificador: " + id);
				return;
			} else {
				System.out.println("Introduzca su contraseña:");
				String passwd = scan.nextLine();

				if (answer.getString("passwd").trim().equals(passwd)) {
					String dni = answer.getString("DNI");
					String dniBien = dni;
					String nombre = answer.getString("Nombre");
					String apellidos = answer.getString("Apellidos");
					String iban = answer.getString("IBAN");
					System.out.println("DNI: " + dni + "\n" + "Nombre: " + nombre + "\n" + "Apellidos: " + apellidos
							+ "\n" + "IBAN: " + iban + "\n");
					System.out.println("¿Desea modificarlos?(S/n):");
					String opcion = scan.nextLine();
					if (opcion.equals("s") || opcion.equals("")) {
						System.out.println("Cambie el valor o pulse enter para dejarlo como estaba");
						System.out.println("DNI:");
						dni = scan.nextLine();
						System.out.println("Nombre:");
						nombre = scan.nextLine();
						System.out.println("Apellidos:");
						apellidos = scan.nextLine();
						System.out.println("IBAN:");
						iban = scan.nextLine();

						sql = "UPDATE Cliente SET ";

						if (!(nombre.equals("")))
							sql = sql + "Nombre='" + nombre + "'";

						if (!(apellidos.equals(""))) {
							if (!(nombre.equals("")))
								sql = sql + ", ";
							sql = sql + "Apellidos='" + apellidos + "'";
						}

						if (!(dni.equals(""))) {
							if (!(nombre.equals("")) || !(apellidos.equals("")))
								sql = sql + ", ";
							sql = sql + "DNI='" + dni + "'";
						}

						if (!(iban.equals(""))) {
							if (!(nombre.equals("")) || !(apellidos.equals("")) || !(dni.equals("")))
								sql = sql + ", ";
							sql = sql + "IBAN='" + iban + "' ";
						}
						if (dni.equals("") && nombre.equals("") && apellidos.equals("") && iban.equals("")) {
							System.out.println("No se ha modificado ningún campo a petición del Cliente.");
						} else {

							sql = sql + " WHERE DNI = '" + dniBien + "'";
							System.out.println(sql);
							state.executeUpdate(sql);
						}

					} else if (opcion.equals("n")) {
						return;
					} else {
						System.out.println("Opción no válida.Datos no modificados.");
					}

				} else {
					System.out.println("No tienes permisos para ver esa información.");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private static void addCliente(Scanner scan, Statement state, String sql) {

		System.out.println("Dni:");
		String dni = scan.nextLine();
		System.out.println("Nombre:");
		String nombre = scan.nextLine();
		System.out.println("Apellidos:");
		String apellidos = scan.nextLine();
		System.out.println("IBAN:");
		String iban = scan.nextLine();
		System.out.println("Contraseña:");
		String passwd = scan.nextLine();
		String apellidosAux[] = apellidos.split(" ");
		if (dni.length() < 9) {
			System.out.println("Error al añadir cliente.La longitud del campo DNI es incorrecta.");
			return;
		} else if (nombre.equals("")) {
			System.out.println("Error al añadir cliente.No se puede dejar un campo en blanco.");
			return;
		} else if (apellidos.equals(" ")) {
			System.out.println("Error al añadir cliente.No se puede dejar un campo en blanco.");
			return;
		} else if (apellidosAux.length < 2) {
			System.out.println("Error al añadir cliente.Debe tener dos apellidos.");
			return;
		} else if (iban.length() < 24) {
			System.out.println("Error al añadir cliente.La longitud del campo IBAN es incorrecta.");
			return;
		}

		sql = "insert into Cliente values ('" + dni + "','" + nombre + "','" + apellidos + "','" + passwd + "','" + iban
				+ "')";
		try

		{
			state.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private static void comprarEntrada(Scanner scan, Statement state, String sql) {

		try {

			System.out.println("¿Qué evento desea consultar la disponibilidad de localidades?\n");
			String consulta = "SELECT ev.idEvento,e.titulo from Espectaculo e\n"
					+ "INNER join Evento ev on ev.idEspectaculo = e.idEspectaculo\n" + "where estado='abierto'";
			String id = null;
			String nombreRecinto = null;
			String idRecinto = null;
			String idGrada = null;
			try {
				ResultSet answer = state.executeQuery(consulta);

				while (answer.next()) {
					System.out.println(
							"id: " + answer.getString("idEvento") + "   Evento: " + answer.getString("titulo") + "\n");
				}
				System.out.println("Introduzca el id:");
				id = scan.nextLine();
				sql = "SELECT DISTINCT r.Nombre FROM Evento_Grada e \n"
						+ "INNER JOIN Recinto r ON r.idRecinto=e.idRecinto\n" + "WHERE e.idEvento='" + id + "'";
				answer = state.executeQuery(sql);
				while (answer.next()) {
					nombreRecinto = answer.getString("Nombre");
					System.out.println(
							"Has seleccionado el evento " + id + " que se celebra en: " + answer.getString("Nombre"));
				}
				sql = "select idRecinto from Recinto where Nombre='" + nombreRecinto + "'";
				answer = state.executeQuery(sql);
				if (answer.next()) {
					idRecinto = answer.getString("idRecinto");
				}
				sql = "select idGrada,Nombre from Grada where idRecinto='" + idRecinto + "'";
				System.out.println("\nIndique en cuál de las siguientes gradas desea consultar\n");
				answer = state.executeQuery(sql);
				while (answer.next()) {
					System.out
							.println("Grada:" + answer.getString("idGrada") + " Nombre: " + answer.getString("Nombre"));
				}
				System.out.println("\nIntroduzca el id de la grada:\n");
				idGrada = scan.nextLine();
				sql = "select idLocalidad from Localidad where idRecinto='" + idRecinto + "' and idGrada='" + idGrada
						+ "'";
				answer = state.executeQuery(sql);
				while (answer.next()) {
					System.out.println("ID Localidad: " + answer.getString("idLocalidad"));
				}
			} catch (SQLException e) {

			}

			System.out.println("Introduzca el id o ids de las localidades que desea comprar: ");
			String idLocalidad = scan.nextLine();
			System.out.println("Introduzca su dni:");
			String dni = scan.nextLine();
			System.out.println("Introduzca el tipo de usuario para el que compra la entrada:");
			String tipo = scan.nextLine();
			String idLocalidadAux[] = idLocalidad.split(" ");
			for (int i = 0; i < idLocalidadAux.length; i++) {
				String query = "call comprar(" + Integer.valueOf(id) + "," + Integer.valueOf(idGrada) + ","
						+ Integer.valueOf(idLocalidadAux[i]) + ",'" + tipo + "','" + dni + "',@idCompra);";
				ResultSet rs = state.executeQuery(query);

				query = "SELECT @idCompra;";
				rs = state.executeQuery(query);
				String idError = null;
				while (rs.next()) {
					idError = rs.getString("@idCompra");
					if (Integer.valueOf(idError) > 0) {
						System.out.println("Resultado de la compra " + (i + 1) + ": " + idError);
					} else {
						query = "select Descripcion from Error where idError=" + Integer.valueOf(idError);
						rs = state.executeQuery(query);
						while (rs.next()) {
							System.out.println(rs.getString("Descripcion") + "\n");
						}
					}
				}
				
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private static boolean comprobarPermisos(Scanner scan) {
		System.out.println("Esta tarea solo la puede realizar el administrador, por favor identificate.");
		System.out.println("Introduce el id de administrador:");
		String id = scan.nextLine();
		System.out.println("Introduce la contraseña:");
		String contra = scan.nextLine();
		if (id.equals(IDADMIN) && contra.equals(PASS)) {
			return true;
		} else {
			System.out.println("Acceso denegado.\n");
			return false;
		}
	}

}
