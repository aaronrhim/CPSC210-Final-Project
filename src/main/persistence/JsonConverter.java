package persistence;

import model.ScalarField;
import model.Simulation;
import model.Vector2;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Handles translating simulations to and from JSON payloads for persistence.
 */
public class JsonConverter {
    private static final String KEY_FIELD = "field";
    private static final String KEY_FIELD_EXPRESSION = "expression";
    private static final String KEY_FIELD_XMIN = "xMin";
    private static final String KEY_FIELD_XMAX = "xMax";
    private static final String KEY_FIELD_YMIN = "yMin";
    private static final String KEY_FIELD_YMAX = "yMax";
    private static final String KEY_FIELD_ZMIN = "zMin";
    private static final String KEY_FIELD_ZMAX = "zMax";

    private static final String KEY_CURRENT_POINT = "currentPoint";
    private static final String KEY_PATH = "path";
    private static final String KEY_LEARNING_RATE = "learningRate";
    private static final String KEY_STOP_THRESHOLD = "stopThreshold";
    private static final String KEY_MAX_ITERATIONS = "maxIterations";
    private static final String KEY_EPS = "eps";
    private static final String KEY_TIME_ELAPSED = "timeElapsed";

    private static final String KEY_VECTOR_X = "x";
    private static final String KEY_VECTOR_Y = "y";

    private JsonConverter() {
        // utility class
    }

    // EFFECTS: converts a simulation to a JSON object
    public static JSONObject simulationToJsonObject(Simulation simulation) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(KEY_LEARNING_RATE, simulation.getLearningRate());
        jsonObject.put(KEY_STOP_THRESHOLD, simulation.getStopThreshold());
        jsonObject.put(KEY_MAX_ITERATIONS, simulation.getMaxIterations());
        jsonObject.put(KEY_EPS, simulation.getEps());
        jsonObject.put(KEY_TIME_ELAPSED, simulation.getTimeElapsed());

        ScalarField field = simulation.getField();
        if (field != null) {
            jsonObject.put(KEY_FIELD, scalarFieldToJson(field));
        }

        Vector2 currentPoint = simulation.getCurrentPoint();
        if (currentPoint != null) {
            jsonObject.put(KEY_CURRENT_POINT, vector2ToJson(currentPoint));
        }

        JSONArray pathArray = new JSONArray();
        for (Vector2 point : simulation.getPath()) {
            pathArray.put(vector2ToJson(point));
        }
        jsonObject.put(KEY_PATH, pathArray);

        return jsonObject;
    }

    // EFFECTS: converts a JSON object into a populated simulation
    public static Simulation jsonObjectToSimulation(JSONObject jsonObject) {
        Simulation simulation = new Simulation();

        simulation.setLearningRate((float) jsonObject.optDouble(KEY_LEARNING_RATE, simulation.getLearningRate()));
        simulation.setStopThreshold((float) jsonObject.optDouble(KEY_STOP_THRESHOLD, simulation.getStopThreshold()));
        simulation.setMaxIterations(jsonObject.optInt(KEY_MAX_ITERATIONS, simulation.getMaxIterations()));
        simulation.setEps((float) jsonObject.optDouble(KEY_EPS, simulation.getEps()));
        simulation.setTimeElapsed((float) jsonObject.optDouble(KEY_TIME_ELAPSED, 0f));

        if (jsonObject.has(KEY_FIELD)) {
            ScalarField field = scalarFieldFromJson(jsonObject.getJSONObject(KEY_FIELD));
            simulation.setField(field);
        }

        if (jsonObject.has(KEY_PATH)) {
            List<Vector2> pathPoints = vector2ListFromJson(jsonObject.getJSONArray(KEY_PATH));
            if (!pathPoints.isEmpty()) {
                simulation.overwritePath(pathPoints);
            }
        }

        if (simulation.getCurrentPoint() == null && jsonObject.has(KEY_CURRENT_POINT)) {
            Vector2 current = vector2FromJson(jsonObject.getJSONObject(KEY_CURRENT_POINT));
            if (current != null && simulation.getField() != null) {
                simulation.setInitialPoint(current.getX(), current.getY());
            }
        }

        return simulation;
    }

    private static JSONObject scalarFieldToJson(ScalarField field) {
        JSONObject json = new JSONObject();
        json.put(KEY_FIELD_EXPRESSION, field.getName());
        json.put(KEY_FIELD_XMIN, field.getXMin());
        json.put(KEY_FIELD_XMAX, field.getXMax());
        json.put(KEY_FIELD_YMIN, field.getYMin());
        json.put(KEY_FIELD_YMAX, field.getYMax());
        json.put(KEY_FIELD_ZMIN, field.getZMin());
        json.put(KEY_FIELD_ZMAX, field.getZMax());
        return json;
    }

    private static ScalarField scalarFieldFromJson(JSONObject json) {
        String expression = json.getString(KEY_FIELD_EXPRESSION);
        ScalarField field = createScalarFieldFromExpression(expression);
        float xMin = (float) json.getDouble(KEY_FIELD_XMIN);
        float xMax = (float) json.getDouble(KEY_FIELD_XMAX);
        float yMin = (float) json.getDouble(KEY_FIELD_YMIN);
        float yMax = (float) json.getDouble(KEY_FIELD_YMAX);
        float zMin = (float) json.getDouble(KEY_FIELD_ZMIN);
        float zMax = (float) json.getDouble(KEY_FIELD_ZMAX);
        field.setDomain(xMin, xMax, yMin, yMax, zMin, zMax);
        return field;
    }

    private static JSONObject vector2ToJson(Vector2 vector) {
        JSONObject json = new JSONObject();
        json.put(KEY_VECTOR_X, vector.getX());
        json.put(KEY_VECTOR_Y, vector.getY());
        return json;
    }

    private static Vector2 vector2FromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        float x = (float) json.getDouble(KEY_VECTOR_X);
        float y = (float) json.getDouble(KEY_VECTOR_Y);
        return new Vector2(x, y);
    }

    private static List<Vector2> vector2ListFromJson(JSONArray array) {
        List<Vector2> list = new ArrayList<>();
        if (array == null) {
            return list;
        }
        for (int i = 0; i < array.length(); i++) {
            JSONObject entry = array.optJSONObject(i);
            if (entry != null) {
                list.add(vector2FromJson(entry));
            }
        }
        return list;
    }

    private static ScalarField createScalarFieldFromExpression(String expr) {
        BiFunction<Float, Float, Float> fn = (Float x, Float y) -> {
            try {
                Expression expression = new ExpressionBuilder(expr)
                        .variables("x", "y")
                        .build()
                        .setVariable("x", x)
                        .setVariable("y", y);

                return (float) expression.evaluate();
            } catch (Exception ex) {
                throw new IllegalArgumentException("Failed to evaluate expression: " + expr, ex);
            }
        };

        return new ScalarField(expr, fn);
    }
}
