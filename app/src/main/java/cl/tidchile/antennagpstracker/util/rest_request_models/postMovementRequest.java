package cl.tidchile.antennagpstracker.util.rest_request_models;

import java.util.ArrayList;

import cl.tidchile.antennagpstracker.models.Movement;

/**
 * Created by benjamin on 3/4/16.
 */
public class PostMovementRequest {
    public ArrayList<Movement> movements;

    public PostMovementRequest(ArrayList<Movement> movements) {
        this.movements = movements;
    }
}
