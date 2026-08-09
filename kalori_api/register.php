<?php

include "db.php";

$email = $_POST['email'];
$password = $_POST['password'];

$checkEmail = "SELECT * FROM users WHERE email='$email'";
$result = $conn->query($checkEmail);

if ($result->num_rows > 0) {
    echo "Email already exists";
} else {

    $hashedPassword = password_hash($password, PASSWORD_DEFAULT);

    $sql = "INSERT INTO users (email, password)
            VALUES ('$email', '$hashedPassword')";

    if ($conn->query($sql) === TRUE) {
        echo "Register Success";
    } else {
        echo "Register Failed";
    }
}

$conn->close();

?>