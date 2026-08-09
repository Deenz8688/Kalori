<?php

include "db.php";

$user_id = $_POST['user_id'];
$meal_type = $_POST['meal_type'];
$food_date = $_POST['food_date'];

$sql = "DELETE FROM foods
WHERE user_id='$user_id'
AND meal_type='$meal_type'
AND food_date='$food_date'";

if ($conn->query($sql) === TRUE) {

    echo "Deleted";

} else {

    echo "Delete Failed";
}

$conn->close();

?>