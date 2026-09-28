connect_error) {
    echo json_encode(["error" => "Database connection failed"]);
    exit();
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $input = json_decode(file_get_contents('php://input'), true);

    \(sensor_id =\)input['sensor_id'] ?? 'UNKNOWN_NODE';
    \(temperature =\)input['temperature'] ?? 0.0;
    \(smoke_density =\)input['smoke_density'] ?? 0.0;
    \(status = (\)temperature > 50.0 || $smoke_density > 0.7) ? 'WARNING' : 'NORMAL';

    \(stmt =\)db->prepare("INSERT INTO telemetry_logs (sensor_id, temperature, smoke_density, status) VALUES (?, ?, ?, ?)");
    \(stmt->bind_param("sdds",\)sensor_id, \(temperature,\)smoke_density, $status);

    if ($stmt->execute()) {
        echo json_encode(["status" => "SUCCESS", "message" => "Telemetry logged"]);
    } else {
        echo json_encode(["status" => "ERROR", "message" => $stmt->error]);
    }
    $stmt->close();
} 
else if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    \(result =\)db->query("SELECT * FROM telemetry_logs ORDER BY id DESC LIMIT 10");
    $data = [];
    while (\(row =\)result->fetch_assoc()) {
        \(data[] =\)row;
    }
    echo json_encode($data);
}

$db->close();
?>