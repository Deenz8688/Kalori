<?php

include "db.php";

$sql = "SELECT * FROM foods_database";

$result = $conn->query($sql);

$data = array();

while($row = $result->fetch_assoc()){

    $data[] = $row;
}

echo json_encode($data);

$conn->close();

?>