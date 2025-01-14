package com.blog;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PostJdbcRepository {
    private static final String URL = "jdbc:mysql://localhost:3306/blog";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public List<Post> findAll() {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT p.id, p.title, p.content, p.author, p.created_at, c.id AS category_id, c.name AS category_name "
                + "FROM post p " + "JOIN category c ON p.category_id = c.id";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                posts.add(create(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return posts;
    }

    public Post save(Post post) {
        String sql = "UPDATE post SET title =?, content =?, author =?, created_at =?, category_id =? WHERE id =?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, post.getTitle());
            preparedStatement.setString(2, post.getContent());
            preparedStatement.setString(3, post.getAuthor());
            preparedStatement.setTimestamp(4, Timestamp.valueOf(post.getCreatedAt()));
            preparedStatement.setLong(5, post.getCategory().getId());

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return post;
    }

    public Post findById(Long id) {
        String sql = "SELECT p.id, p.title, p.content, p.author, p.created_at, c.id AS category_id, c.name AS category_name "
                + "FROM post p " + "JOIN category c ON p.category_id = c.id " + "WHERE p.id = ?";
        Post post = null;

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    post = create(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return post;
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM post WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Post create(ResultSet resultSet) throws SQLException {
        Post post = new Post();
        post.setId(resultSet.getLong("id"));
        post.setTitle(resultSet.getString("title"));
        post.setContent(resultSet.getString("content"));
        post.setAuthor(resultSet.getString("author"));
        post.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());

        Category category = new Category();
        category.setId(resultSet.getLong("category_id"));
        category.setName(resultSet.getString("category_name"));
        post.setCategory(category);
        return post;
    }
}