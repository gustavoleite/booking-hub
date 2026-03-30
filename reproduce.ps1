$headers = @{
    "X-User-Id" = "f9cb117e-6e8e-4de8-af00-b9ac2bddb223"
    "Content-Type" = "application/json"
}
$body = @{
    name = "Maria Silva"
    bio = "Esteticista experiente"
    avatarUrl = "http://example.com/maria.jpg"
    specialties = @("Estética Facial")
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8083/api/catalog/professionals/me" -Method Put -Headers $headers -Body $body
