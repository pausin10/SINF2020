USE taquillavirtual;
DELIMITER //
DROP PROCEDURE IF EXISTS anular//
CREATE PROCEDURE anular(IN ID_Evento int, IN ID_Grada int, IN ID_Localidad int, IN DNI varchar(30), OUT Penalizacion int)
p_anular:BEGIN

    DECLARE current TIMESTAMP;
    DECLARE fecha_hora TIMESTAMP;
    DECLARE fecha DATE;
    DECLARE hora TIME;
    DECLARE minute int;
    DECLARE penal int;
    DECLARE T3 int;
    DECLARE id_compra int;
    DECLARE id_recinto int;

    SET @obtener_id = concat("SELECT Pagar_Prereservar.idCompra into @id_compra from Pagar_Prereservar 
    where Pagar_Prereservar.idEvento = ", ID_Evento, " and Pagar_Prereservar.idGrada = " , ID_Grada," and Pagar_Prereservar.idLocalidad = ", ID_Localidad," and Pagar_Prereservar.dniCliente = '",DNI,"';");
    PREPARE stmt_obtener_id FROM @obtener_id;
    EXECUTE stmt_obtener_id;

    SET @obtener_id = concat("SELECT Pagar_Prereservar.idRecinto into @id_recinto from Pagar_Prereservar where Pagar_Prereservar.idEvento = ", ID_Evento, " and Pagar_Prereservar.idGrada = " , ID_Grada,
    " and Pagar_Prereservar.idLocalidad = ", ID_Localidad," and Pagar_Prereservar.dniCliente = '",DNI,"';");
    PREPARE stmt_obtener_id FROM @obtener_id;
    EXECUTE stmt_obtener_id;

    IF @id_compra IS NOT NULL THEN

        SET @obtener_t3 = concat("SELECT Evento.TimePenaliza into @T3 from Evento where Evento.idEvento = ",ID_Evento, " ;");
        PREPARE stmt_obtener_t3 FROM @obtener_t3;
        EXECUTE stmt_obtener_t3;

        SET @obtener_t3 = concat("SELECT Evento.Fecha into @fecha from Evento where Evento.idEvento = ",ID_Evento, " ;");
        PREPARE stmt_obtener_t3 FROM @obtener_t3;
        EXECUTE stmt_obtener_t3;

        SET @obtener_t3 = concat("SELECT Evento.Hora into @hora from Evento where Evento.idEvento = ",ID_Evento, " ;");
        PREPARE stmt_obtener_t3 FROM @obtener_t3;
        EXECUTE stmt_obtener_t3;

        SET @obtener_t3 = concat("SELECT Evento.Penalizacion into @penal from Evento where Evento.idEvento = ",ID_Evento, " ;");
        PREPARE stmt_obtener_t3 FROM @obtener_t3;
        EXECUTE stmt_obtener_t3;

        SET @current = CURRENT_TIMESTAMP();
        SET @fecha_hora = concat(@fecha,' ',@hora);
        SET @minute = (SELECT TIMESTAMPDIFF(MINUTE, @current, @fecha_hora));

        SET Penalizacion = 0;
        IF (@minute < 0) THEN
            SET Penalizacion = -11; #Anulacion no valida
            LEAVE p_anular;
        END IF;
        
        IF (@minute < @T3) THEN
            SET Penalizacion = @penal;
        END IF;
        
        SET @borrar = concat("DELETE FROM Pagar_Prereservar WHERE idEvento = ", ID_Evento," and idRecinto = ", @id_recinto," and idGrada = ",ID_Grada," and idLocalidad = ",ID_Localidad," and dniCliente = '",DNI,"' ;");
        PREPARE stmt_borrar FROM @borrar;
        EXECUTE stmt_borrar;
    ELSE 
        SET Penalizacion = -11; #Anulacion no valida
    END IF;
END//
delimiter ;