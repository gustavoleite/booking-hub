$headers = @{
    "Authorization" = "Bearer eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJmOWNiMTE3ZS02ZThlLTRkZTgtYWYwMC1iOWFjMmJkZGIyMjMiLCJyb2xlIjoiUk9MRV9DTElFTlQiLCJleHAiOjE3NzQ4MzIyNzksImlhdCI6MTc3NDgyODY3OSwiZW1haWwiOiJjbGllbnRlQHRlc3RlLmNvbSJ9.g9DtBCKobocV3KZbHO5nyBktqPM07r96cylEG-ILVl2A6SnK6LnJ31r1whU2T6tjFBsJbkWGV3DF3f5T1-6aRHhKMT_9_wZurP9fQrc4GiWuKty_iVv4ebEBoMOcSYDOcEoWKwnjdwBoEjJFUUxVAKczf_bv4SQRa1Nq2bYLoN8EfzZZrb54DRvd5XpXonWUp9HY5sRvqzdk0V31NRQUU3zeH1dWgLXNYDrg2YYqXj_c3HFKwfFLvkl-pKDKqq70Wl1MO8V4QvtJyi1nCVhkIPKMKS5A7qONFGbAmTojRyr7n3Kgr1SsZmuTZBRPZY6CWNlqmjDCUsWZ4KWJ0uetzA"
    "Content-Type" = "application/json"
}
$body = @{
    name = "Maria Silva"
    bio = "Esteticista experiente"
    avatarUrl = "http://example.com/maria.jpg"
    specialties = @("Estética Facial")
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/catalog/professionals/me" -Method Put -Headers $headers -Body $body
