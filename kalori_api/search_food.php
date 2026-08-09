<?php

include "db.php";

$search = $_GET['q'];

$sql = "SELECT * FROM foods_database 
WHERE LOWER(Makanan) LIKE LOWER('%$search%')
LIMIT 20";

$result = $conn->query($sql);

$data = array();

while($row = $result->fetch_assoc()){

    $data[] = $row;
}

echo json_encode($data);

$conn->close();

?>