package com.example.repository.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import com.example.repository.UserRepository;

import javax.sql.DataSource;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final SimpleJdbcCall checkPhoneExistsCall;
    private final SimpleJdbcCall registerUserCall;
    private final SimpleJdbcCall loginUserCall;
    private final SimpleJdbcCall updateLastLoginCall;

    public UserRepositoryImpl(DataSource dataSource) {
        this.checkPhoneExistsCall = new SimpleJdbcCall(dataSource)
            .withSchemaName("dbo")
            .withProcedureName("sp_check_phone_exists");

        this.registerUserCall = new SimpleJdbcCall(dataSource)
            .withSchemaName("dbo")
            .withProcedureName("sp_register_user");

        this.loginUserCall = new SimpleJdbcCall(dataSource)
            .withSchemaName("dbo")
            .withProcedureName("sp_login_user");

        this.updateLastLoginCall = new SimpleJdbcCall(dataSource)
            .withSchemaName("dbo")
            .withProcedureName("sp_update_last_login");
    }

    @Override
    public boolean checkPhoneExists(String phoneNumber) {
        Map<String, Object> result = checkPhoneExistsCall.execute(
            new MapSqlParameterSource("PhoneNumber", phoneNumber)
        );

        List<Map<String, Object>> rows = extractRows(result);
        if (rows.isEmpty()) {
            return false;
        }

        Object value = rows.get(0).get("ExistsFlag");
        if (value instanceof Boolean boolValue) {
            return boolValue;
        }
        if (value instanceof Number number) {
            return number.intValue() == 1;
        }
        return false;
    }

    @Override
    public int registerUser(String phoneNumber, String passwordHash, String passwordSalt, String userName) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("PhoneNumber", phoneNumber)
            .addValue("PasswordHash", passwordHash)
            .addValue("PasswordSalt", passwordSalt)
            .addValue("UserName", userName);

        Map<String, Object> result = registerUserCall.execute(params);
        List<Map<String, Object>> rows = extractRows(result);

        if (rows.isEmpty() || !rows.get(0).containsKey("UserId")) {
            throw new IllegalStateException("sp_register_user did not return UserId");
        }

        Object value = rows.get(0).get("UserId");
        if (value instanceof Number number) {
            return number.intValue();
        }
        throw new IllegalStateException("UserId is not numeric");
    }

    @Override
    public Optional<Map<String, Object>> findLoginUserByPhone(String phoneNumber) {
        Map<String, Object> result = loginUserCall.execute(
            new MapSqlParameterSource("PhoneNumber", phoneNumber)
        );

        List<Map<String, Object>> rows = extractRows(result);
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(rows.get(0));
    }

    @Override
    public void updateLastLogin(int userId) {
        updateLastLoginCall.execute(new MapSqlParameterSource("UserId", userId));
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractRows(Map<String, Object> result) {
        Object rows = result.get("#result-set-1");
        if (rows instanceof List<?>) {
            return (List<Map<String, Object>>) rows;
        }
        return List.of();
    }
}
