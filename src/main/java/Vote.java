import static spark.Spark.*;

import java.util.HashMap;
import java.util.Map;

import spark.ModelAndView;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import org.json.simple.*;

@SuppressWarnings("unchecked")
public class Vote {
  private static final String STATIC_FILE_LOCATION = "/public";

  private static Map<String, Integer> candVotes = getInitialMap();

  private static Integer totalVotes = 0;

  @SuppressWarnings("serial")
  public static void main(String[] args) {

    port(getHerokuAssignedPort());
    Spark.staticFileLocation(STATIC_FILE_LOCATION);


    get("/", (req, res) -> new ModelAndView(candVotes, "index.mustache"), new MustacheTemplateEngine());

    get("/votes", (req, res) -> {
      JSONObject jsonObj = new JSONObject() {{
        putAll(candVotes);
      }};

      return jsonObj.toJSONString();
    });

    get("/votes/:name", (req, res) -> {
      String name = req.params(":name");

      Integer numVotes = candVotes.get(name);

      if (numVotes == null) {
        halt(404, "Candidate " + name + " cannot be voted on.");
      }

      return numVotes;
    });

    get("/percent/:name", (req, res) -> {
      String name = req.params(":name");

      Integer numVotes = candVotes.get(name);

      if (numVotes == null) {
        halt(404, "Candidate " + name + " cannot be voted on.");
      }

      return (double) numVotes / totalVotes;
    });

    post("/voter/:name", (req, res) -> {
      String name = req.params(":name");

      if (!candVotes.containsKey(name)) {
        halt(404, "Candidate " + name + " cannot be voted on.");
      }

      // Increment Vote
      candVotes.put(name, candVotes.get(name) + 1);

      JSONObject jsonObj = new JSONObject() {{
        put(name, candVotes.get(name));
      }};

      totalVotes++;

      res.redirect("/");

      return jsonObj.toJSONString();
    });

    post("/restart", (req, res) -> {

      candVotes = getInitialMap();

      totalVotes = 0;

      return "Successfully Restarted Server";
    });
  }



  private static int getHerokuAssignedPort() {
      ProcessBuilder processBuilder = new ProcessBuilder();
      if (processBuilder.environment().get("PORT") != null) {
          return Integer.parseInt(processBuilder.environment().get("PORT"));
      }
      return 4567; //return default port if heroku-port isn't set (i.e. on localhost)
  }

  @SuppressWarnings("serial")
  private static Map<String, Integer> getInitialMap() {
    return new HashMap<String, Integer>() {{
      put("trump", 0);
      put("hillary", 0);
      put("bernie", 0);
      put("cruz", 0);
    }};
  }
  
}
