package cl.tidchile.antennagpstracker.util.rest_response_models;

/**
 * Created by benjamin on 3/4/16.
 */
public class PostMovementResponse {
    public String error;
    public String status;

    public PostMovementResponse(String status, String error) {
        this.status = status;
        this.error = error;
    }
}
