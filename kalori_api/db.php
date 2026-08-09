<?php

$host = "localhost";
$dbname = "u561476065_kalori_db";
$username = "u561476065_admin_kalori";
$password = "Deenz_8688";

$conn = new mysqli($host, $username, $password, $dbname);

if ($conn->connect_error) {
    die("Connection Failed: " . $conn->connect_error);
}

?>