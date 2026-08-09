<?php

include "db.php";

$user_id = $_POST['user_id'];

$sql = "SELECT * FROM profiles WHERE user_id='$user_id'";

$result = $conn->query($sql);

if ($result->num_rows > 0) {

    $row = $result->fetch_assoc();

    echo json_encode([
        "status" => "success",

        "full_name" => $row['full_name'],
        "profile_image" => $row['profile_image'],
        "activity_level" => $row['activity_level'],
        "gender" => $row['gender'],
        "age" => $row['age'],
        "weight" => $row['weight'],
        "height" => $row['height'],
        "bmi" => $row['bmi'],
        "bmr" => $row['bmr'],
        "tdee" => $row['tdee']
    ]);

} else {

    echo json_encode([
        "status" => "no_data"
    ]);
}

$conn->close();

?>