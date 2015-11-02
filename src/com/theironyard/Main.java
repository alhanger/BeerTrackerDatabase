package com.theironyard;

import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    static void insertBeer(Connection conn, String name, String type) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO beers (name, type) VALUES (?, ?)");
        stmt.setString(1, name);
        stmt.setString(2, type);
        stmt.execute();
    }

    static void deleteBeer(Connection conn, int selectNum) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM beers WHERE id = ?");
        stmt.setInt(1, selectNum);
        stmt.execute();
    }

    static ArrayList<Beer> selectBeer (Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet results = stmt.executeQuery("SELECT * FROM beers");
        ArrayList<Beer> beers = new ArrayList<>();
        while (results.next()) {
            int id = results.getInt("id");
            String name = results.getString("name");
            String type = results.getString("type");
            Beer beer = new Beer(id, name, type);
            beers.add(beer);
        }
        return beers;
    }

    static void editBeer(Connection conn, int idNum, String name, String type) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("UPDATE beers SET name = ?, type = ? WHERE id = ?");
        stmt.setString(1, name);
        stmt.setString(2, type);
        stmt.setInt(3, idNum);
        stmt.execute();
    }

    public static void main(String[] args) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS beers (name VARCHAR, type VARCHAR, id IDENTITY)");

        Spark.get(
                "/",
                ((request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");
                    if (username == null) {
                        return new ModelAndView(new HashMap(), "not-logged-in.html");
                    }
                    HashMap m = new HashMap();
                    m.put("username", username);
                    m.put("beers", selectBeer(conn));
                    return new ModelAndView(m, "logged-in.html");
                }),
                new MustacheTemplateEngine()
        );
        Spark.post(
                "/login",
                ((request, response) -> {
                    String username = request.queryParams("username");
                    Session session = request.session();
                    session.attribute("username", username);
                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/create-beer",
                ((request, response) -> {
                    //Beer beer = new Beer();
                    //beer.id = beers.size() + 1;
                    String name = request.queryParams("beername");
                    String type = request.queryParams("beertype");
                    insertBeer(conn, name, type);
                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/delete-beer",
                ((request, response) -> {
                    String id = request.queryParams("beerid");
                    int idNum = Integer.valueOf(id);

                    deleteBeer(conn, idNum);

//                    try {
//                        beers.remove(idNum-1);
//                        for (int i = 0; i < beers.size(); i++) {
//                            beers.get(i).id = i + 1;
//                        }
//                    } catch (Exception e) {
//
//                    }

                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "edit-beer",
                ((request, response) -> {
                    String id = request.queryParams("beerid");
                    int idNum = Integer.valueOf(id);
                    String name = request.queryParams("beername");
                    String type = request.queryParams("beertype");

                    editBeer(conn, idNum, name, type);

                    response.redirect("/");
                    return "";
                })
        );
    }
}
