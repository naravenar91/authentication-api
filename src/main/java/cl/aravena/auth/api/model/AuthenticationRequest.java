package cl.aravena.auth.api.model;

public record AuthenticationRequest(
        String uuid,
        String userName,
        String password,
        Boolean isActive
) {

    public AuthenticationRequest newAuth(AuthenticationRequest request){
        return new AuthenticationRequest(uuid, userName, password, isActive);
    }

    public AuthenticationRequest login(AuthenticationRequest request){
        return new AuthenticationRequest(null, userName, password, true);
    }
}