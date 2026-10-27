$ErrorActionPreference = "Stop"
$gradleFile = Join-Path $PSScriptRoot "app\build.gradle.kts"
$content = Get-Content $gradleFile -Raw

$codeMatch = [regex]::Match($content, 'versionCode\s*=\s*(\d+)')
$nameMatch = [regex]::Match($content, 'versionName\s*=\s*"(\d+)\.(\d+)\.(\d+)"')

if (-not $codeMatch.Success -or -not $nameMatch.Success) {
    Write-Host "Could not find versionCode/versionName in $gradleFile" -ForegroundColor Red
    exit 1
}

$oldCode = [int]$codeMatch.Groups[1].Value
$newCode = $oldCode + 1

$major = [int]$nameMatch.Groups[1].Value
$minor = [int]$nameMatch.Groups[2].Value
$patch = [int]$nameMatch.Groups[3].Value + 1
$newName = "$major.$minor.$patch"
$oldName = "$major.$minor.$($nameMatch.Groups[3].Value)"

$content = $content -replace 'versionCode\s*=\s*\d+', "versionCode = $newCode"
$content = $content -replace 'versionName\s*=\s*"\d+\.\d+\.\d+"', "versionName = `"$newName`""

Set-Content -Path $gradleFile -Value $content -NoNewline -Encoding utf8

Write-Host "Version bumped: $oldCode ($oldName) -> $newCode ($newName)" -ForegroundColor Green
