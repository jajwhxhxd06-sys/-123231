local player = game:GetService("Players").LocalPlayer
local gui = Instance.new("ScreenGui")
gui.Parent = player.PlayerGui

local frame = Instance.new("Frame")
frame.Size = UDim2.new(0.3, 0, 0.2, 0)
frame.Position = UDim2.new(0.5, -150, 0.5, -100)
frame.BackgroundColor3 = Color3.new(0, 0, 0)
frame.BackgroundTransparency = 0.5
frame.Parent = gui

local label = Instance.new("TextLabel")
label.Text = "Инжектировано!"
label.TextColor3 = Color3.new(1, 1, 1)
label.Size = UDim2.new(1, 0, 1, 0)
label.BackgroundTransparency = 1
label.Parent = frame

print("Скрипт выполнен успешно!")
