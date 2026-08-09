<?php

include "db.php";

$user_id = $_GET['user_id'];

$sql = "SELECT * FROM foods 
WHERE user_id='$user_id'
ORDER BY created_at DESC";

$result = $conn->query($sql);

$data = array();

while($row = $result->fetch_assoc()) {

    $data[] = $row;
}

echo json_encode($data);

$conn->close();

?>