<?php

include "db.php";

$email = $_POST['email'];
$password = $_POST['password'];

$sql = "SELECT * FROM users WHERE email='$email'";
$result = $conn->query($sql);

if ($result->num_rows > 0) {

    $row = $result->fetch_assoc();

    if (password_verify($password, $row['password'])) {

        echo json_encode([
            "status" => "success",
            "user_id" => $row['id'],
            "email" => $row['email']
        ]);

    } else {
        echo "Wrong Password";
    }

} else {
    echo "User Not Found";
}

$conn->close();

?>