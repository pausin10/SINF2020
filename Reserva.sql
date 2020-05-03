USE taquillavirtual;
DELIMITER //
DROP PROCEDURE IF EXISTS reservar//
CREATE PROCEDURE reservar(IN ID_Evento int, IN ID_Grada int, IN ID_Localidad int, IN tipoUsuario varchar(10), in DNI varchar(9), OUT idCompra int)

/*pagar la prereserva*/
/********************************************/
/*Meteria otro valor de OUT que sea el de error, ahora mismo idCompra saca, si es correcto, la compra o si fallo el error*/
/********************************************/

BEGIN

    DECLARE current TIMESTAMP;
    DECLARE fecha_hora TIMESTAMP;
    DECLARE fecha DATE;
    DECLARE hora TIME;
    DECLARE minute INT;
    DECLARE T2 int;
    DECLARE usuario VARCHAR(10);
    DECLARE dni_cliente VARCHAR(9);
    DECLARE estado_localidad VARCHAR(11);
    DECLARE estado_evento VARCHAR(11);
    DECLARE entrada_pagada INT;
    DECLARE mismo_usuario INT;
    DECLARE entradas_prerreservadas INT;
    DECLARE max_prerreservas INT;
    DECLARE cursorUsuario CURSOR FOR SELECT Evento_Grada.Usuario FROM Evento_Grada WHERE Evento_Grada.idEvento = ID_Evento AND Evento_Grada.idGrada = ID_Grada;

    SET @id_recinto = 0;
    SET @id_localidad = 0;
    SET @id_grada = 0;
    SET @id_evento = 0;
	SET @entrada_pagada = 0;
    SET @entrada = 0;
    SET @entradas_prerreservadas = 0;

    SET @select1 = concat("SELECT Ventas.idCompra into @entrada from Ventas WHERE Ventas.idEvento =  ", ID_Evento,
    " and Ventas.idGrada = ", ID_Grada, " and Ventas.idLocalidad =  ", ID_Localidad, ";");

	SET @obtencion_recinto = concat("SELECT DISTINCT Evento_Grada.idRecinto into @id_recinto from Evento_Grada where Evento_Grada.idEvento = ", ID_Evento, ";");
	PREPARE stmt_obt_recinto FROM @obtencion_recinto;
	EXECUTE stmt_obt_recinto;

    SET @estado = concat("SELECT Localidad.Estado into @estado_localidad from Localidad
     WHERE Localidad.idGrada = ", ID_Grada, " and Localidad.idLocalidad = ", ID_Localidad," and Localidad.idRecinto = ", @id_recinto, ";");
    PREPARE stmt_estado FROM @estado; #Comprobamos estado localidad
    EXECUTE stmt_estado;

    SET @current  = now();
    SET @aux = concat("SELECT Fecha into @fecha FROM Evento WHERE Evento.idEvento = ",ID_Evento,";");
    PREPARE stmt_fecha FROM @aux;
    EXECUTE stmt_fecha;
    SET @aux2 = concat("SELECT Hora into @hora FROM Evento WHERE Evento.idEvento = ",ID_Evento,";");
    PREPARE stmt_hora FROM @aux2;
    EXECUTE stmt_hora;

    SET @fecha_hora = concat(@fecha,' ',@hora);
    SET @minute = TIMESTAMPDIFF(MINUTE, @current, @fecha_hora); 
    SET @compra = concat("SELECT TiempoAnulacion into @T2 from Evento WHERE idEvento = ", ID_Evento, ";");
    PREPARE stmt_aux FROM @compra;
    EXECUTE stmt_aux;

    /*T2 valor de tiempo del comienzo de un espectaculo*/
    /*Comprobacion de si cuando queremos pagar una localidad, el usuario podra realizar dicha accion*/

    IF (@minute < @T2) THEN
        SET idCompra = -7;
    ELSE 
        IF @estado_localidad = "Deteriorado" THEN 
            SET @evento = concat("SELECT Evento.Estado into @estado_evento from Evento WHERE Evento.idEvento = ", ID_Evento, ";");
            PREPARE stmt_evento FROM @evento;
            EXECUTE stmt_evento;

            IF @estado_evento = "Cerrado" OR @estado_evento = "Finalizado" THEN
                SET idCompra = -6; #evento cerrado o Finalizado
            ELSE 
                SET idCompra = -1; #localidad deteriorada                     
            END IF;
        ELSE 

            SET @compra = concat("SELECT dniCliente into @dni_cliente from Ventas
            WHERE idLocalidad = ", ID_Localidad, " and idGrada = ", ID_Grada, " and idEvento = ", ID_Evento, ";");
            PREPARE stmt_dni FROM @compra;
            EXECUTE stmt_dni;

            IF @dni_cliente IS NULL THEN #entrada libre
                open cursorUsuario;
                BEGIN 
                    DECLARE done BOOLEAN DEFAULT FALSE;
                    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
                    bucleUsuario: LOOP
                        FETCH cursorUsuario into usuario;
                        IF done THEN  
                            SET idCompra = -5; #usuarionopermitido 
                            LEAVE bucleUsuario;
                        ELSE 
                            SET @mismo_usuario = 0;
                            SET @comp = concat("SELECT strcmp('",usuario,"','",tipoUsuario,"') into @mismo_usuario;"); #usuario iguales? 
                            PREPARE stmt_comp from @comp;
                            EXECUTE stmt_comp;
    
                            IF @mismo_usuario = 0 THEN
                                SET @evento = concat("SELECT Evento.Estado into @estado_evento from Evento WHERE Evento.idEvento = ", ID_Evento, ";");
                                PREPARE stmt_evento FROM @evento;
                                EXECUTE stmt_evento;
                                IF @estado_evento = "Cerrado" OR @estado_evento = "Finalizado" THEN
                                    SET idCompra = -6; #evento cerrado o Finalizado
                                    LEAVE bucleUsuario;
                                ELSE 
                                    SET @prerreserva = concat("SELECT COUNT(*) into @entradas_prerreservadas FROM Ventas 
                                    WHERE Ventas.idEvento = ", ID_Evento, " and Ventas.idGrada = ", ID_Grada, " and Ventas.dniCliente = '"
                                    , DNI,"' and Ventas.pagada = 0;");
                                    PREPARE stmt_aforo FROM @prerreserva;
                                    EXECUTE stmt_aforo;
                                    SET @max = concat("select distinct Grada.MaxPrereserva into @max_prerreservas from Grada natural join Evento_Grada 
                                    where Evento_Grada.idEvento= ",ID_Evento," and Evento_Grada.idGrada = ",ID_Grada,";");

                                    PREPARE stmt_aforo FROM @max;
                                    EXECUTE stmt_aforo;

                                    IF (@entradas_prerreservadas >= @max_prerreservas) THEN 
                                        SET idCompra = -10;
                                        LEAVE bucleUsuario;
                                    ELSE 
                                        SET @aforo = concat("SELECT COUNT(*) into @ventas_grada FROM Ventas 
                                        WHERE Ventas.idEvento = ", ID_Evento, " and Ventas.idGrada = ", ID_Grada, ";");
                                        PREPARE stmt_aforo FROM @aforo;
                                        EXECUTE stmt_aforo;
                                        SET @aforo2 = concat("select distinct Grada.CapacidadMax into @aforo_grada from Grada natural join Evento_Grada 
                                        where Evento_Grada.idEvento= ",ID_Evento," and Evento_Grada.idGrada = ",ID_Grada,";");
                                        PREPARE stmt_aforo FROM @aforo2;
                                        EXECUTE stmt_aforo;
                                        
                                        IF (@ventas_grada >= @aforo_grada) THEN 
                                            SET idCompra = -9;
                                            LEAVE bucleUsuario;
                                        ELSE 
                                            SET @aforo = concat("SELECT COUNT(*) into @ventas_recinto FROM Ventas 
                                            WHERE Ventas.idEvento = ", ID_Evento, " and Ventas.idGrada = ", ID_Grada, ";");
                                            PREPARE stmt_aforo FROM @aforo;
                                            EXECUTE stmt_aforo;
                                            SET @aforo2 = concat("select distinct Recinto.CapacidadMax into @aforo_recinto from Recinto natural join Evento_Grada 
                                            where Evento_Grada.idEvento= ",ID_Evento," and Evento_Grada.idRecinto = ",@id_recinto,";");
                                            PREPARE stmt_aforo FROM @aforo2;
                                            EXECUTE stmt_aforo;

                                            IF (@ventas_recinto >= @aforo_recinto) THEN 
                                                SET idCompra = -9;
                                                LEAVE bucleUsuario;
                                            ELSE
                                                INSERT INTO Pagar_Prereservar VALUES(null, DNI, ID_Evento, @id_recinto, ID_Grada, ID_Localidad, now(), 1);
                                                PREPARE id_entrada FROM @select1;
                                                EXECUTE id_entrada;
                                                SET idCompra = @entrada;
                                                LEAVE bucleUsuario;
                                            END IF;
                                        END IF;
                                    END IF;
                                END IF;
                            END IF;
                        END IF;
                    END LOOP;
                END;
                CLOSE cursorUsuario;
            ELSEIF @dni_cliente = DNI THEN #entrada prerreservada por el jicho o comprada
                SET @compra = concat("SELECT pagada into @entrada_pagada from Ventas WHERE idLocalidad = ", ID_Localidad, " and idGrada = ", ID_Grada, " and idEvento = ", ID_Evento, ";");
                PREPARE stmt_dni FROM @compra;
                EXECUTE stmt_dni;

                IF @entrada_pagada = 1 THEN  
                    SET idCompra = -3; #localidad ya comprada
                ELSE 
                    SET idCompra = -8; #localidad ya prerreservada
                END IF;    
            ELSE #entrada ya comprada o prerreservada
                SET idCompra = -2; #localidad ocupada
            END IF;
        END IF;
    END IF;
END//
DELIMITER ;
