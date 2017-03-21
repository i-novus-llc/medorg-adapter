package ru.inovus.egisz.medorg.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inovus.egisz.medorg.rest.RestCallbackCommand;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Stateless
public class MessageInfoService {

    private static final Logger logger = LoggerFactory.getLogger(MessageInfoService.class);

    @Resource(lookup = "java:/jdbc/medorg_adapter")
    private DataSource dataSource;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void addMessage(String messageId, RestCallbackCommand command) {
        executeUpdate("insert into message_queue(id, consumer, callback_url) values (?, ?, ?)", messageId, command.getAuthorizedUserName(), command.getCallbackUrl());
    }

    public RestCallbackCommand getConsumerByMessageId(String messageId) {
        List<List<String>> list = executeQuery("select consumer, callback_url from message_queue where id = ?", messageId);

        RestCallbackCommand restCallbackCommand = null;
        if (!list.isEmpty()) {
            List<String> row = list.get(0);
            restCallbackCommand = new RestCallbackCommand(row.get(0), row.get(1));
        }

        return restCallbackCommand;
    }

    @Asynchronous
    public void deleteMessage(String messageId) {
        executeUpdate("update message_queue set deleted = current_timestamp where id = ?", messageId);
    }

    public int removeOldMessages(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return executeUpdate("delete from message_queue where deleted < to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS.MS')", dateFormat.format(date));
    }

    private int executeUpdate(String sql, String... args) {
        int result = 0;
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement(sql);

            for (int i = 1; i <= args.length; i++) {
                statement.setString(i, args[i-1]);
            }
            result = statement.executeUpdate();
        } catch (SQLException e) {
            logger.warn("MEDORG. Ошибка при выполнении запроса '{}', аргументы: ", sql, args, e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    logger.info("Cannot close statement", e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.info("Cannot close connection", e);
                }
            }
        }
        return result;
    }

    private List<List<String>> executeQuery(String sql, String... args) {
        Connection connection = null;
        PreparedStatement statement = null;
        List<List<String>> result = new ArrayList<>();
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement(sql);

            for (int i = 1; i <= args.length; i++) {
                statement.setString(i, args[i-1]);
            }

            ResultSet resultSet = statement.executeQuery();
            int columnCount = resultSet.getMetaData().getColumnCount();
            while (resultSet.next()) {
                List<String> row = new ArrayList<>();
                for (int i = 0; i < columnCount; i++) {
                    row.add(resultSet.getString(i + 1));
                }
                result.add(row);
            }
        } catch (SQLException e) {
            logger.warn("MEDORG. Ошибка при выполнении запроса '{}', аргументы: ", sql, args, e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    logger.info("Cannot close statement", e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.info("Cannot close connection", e);
                }
            }
        }
        return result;
    }
}
