;##################
;# INITIALIZATION #
;##################
.thetop 
; Set Graphics Mode
Graphics3D 640,480,32,2

; Create a timer to smooth frames
timer = CreateTimer(30)

; Load High Score
If ReadFile("score.dat") = 0 Then
	highScore = 10
Else
	file_score = ReadFile("score.dat")
	highScore = ReadInt(file_score)
	CloseFile(file_score)
EndIf


;#################
;# ASSET LOADING #
;#################

myFont = LoadFont("font/Bitsumishi.ttf", 12, True, 0, 0)
SetFont myFont

; Load images w/ error checking
AutoMidHandle True
img_ship = LoadImage("img/ship.png")
If img_ship = 0 Then RuntimeError("Ship image not loaded!")
img_bullet = LoadImage("img/bullet.png")
If img_bullet = 0 Then RuntimeError("Bullet image not loaded!")
img_alien = LoadImage("img/alien.png")
If img_alien = 0 Then RuntimeError("Alien image not loaded!")
img_bomb = LoadImage("img/bomb.png")
If img_bomb = 0 Then RuntimeError("Bomb image not loaded!")
img_fire = LoadImage("img/Fire.jpg")
ScaleImage img_fire, 0.5, 0.5
If img_fire = 0 Then RuntimeError("Fire image not loaded!")

;img_intro = LoadImage("img/intro_screen.png")
;If img_intro = 0 Then RuntimeError("Intro Screen image not loaded!")


; Background Image
bkgd_space = LoadImage("img/space.jpg")
If bkgd_space = 0 Then RuntimeError("Background image not loaded!")


; Load sounds w/ error checking
snd_shoot = LoadSound("audio/shoot.wav")
If snd_shoot = 0 Then RuntimeError("Shoot sound not loaded!")
snd_shipHit = LoadSound("audio/ship_hit.wav")
If snd_shipHit = 0 Then RuntimeError("Ship Hit sound not loaded!")
snd_bomb = LoadSound("audio/bomb.wav")
If snd_bomb = 0 Then RuntimeError("Bomb sound not loaded!")
snd_explode = LoadSound("audio/explode.wav")
If snd_explode = 0 Then RuntimeError("Explode sound not loaded!")
snd_music = LoadSound("audio/red_doors_2.mp3")
If snd_music = 0 Then RuntimeError("Music not loaded!")
snd_Explosdion = LoadSound("audio/Explosion.wav")

;#####################
;# TYPE DECLARATIONS #
;#####################

; Bullet type
Type bullet
	Field x
	Field y
End Type	

; Bomb type
Type bomb
	Field x
	Field y
End Type

; Alien type
Type alien
	Field x
	Field y
End Type	

; Particle type
Type particle
	Field x
	Field y
	Field mx
	Field my
	Field life
End Type



;######################
;# STARTING VARIABLES #
;######################

; Set starting coordinates for player
x = 320
y = 380

; Set number of lives
lives = 3

; Set score
score = 0

; Set alien speed
aspeed = 2
amx = aspeed
chdir = False

; Initialize numAliens
numAliens = 0

; Starting level
level = 0

; Set drawing buffer to backbuffer
SetBuffer BackBuffer()

; Generate aliens
For z = 1 To 6
	For w = 1 To 2
		a.alien = New alien
		a\x = 100 + (65 * z)
		a\y = 10 + (80 * w)
	Next	
Next

; Setup game music
chnWav = PlaySound(snd_music)
LoopSound snd_music
ChannelVolume chnWav, 0.4

Function ParticleGenerator()
	p.particle = New particle
	angle = Rand(360)
	p\x = 320
	p\y = 240
	p\mx = Cos(angle)
	p\my = Sin(angle)
	p\life = Rand(100, 200)
	For p.particle = Each particle
		p\x = p\x + p\mx * 2
		p\y = p\y + p\my * 2
		p\life = p\life - 1
		Color 50 + p\life, p\life, 0
		Rect p\x, p\y, 2, 2
		If p\life = 0 Then 
			Delete p
		EndIf
	Next
End Function

Function IntroScreen()
	Repeat
		FreeImage img_intro
		Cls
		img_intro = LoadImage("img/intro_screen.jpg")
		If img_intro = 0 Then RuntimeError("Intro image did not load!")
		
		DrawImage img_intro, 320, 240
		Color 12, 255, 0
		Text 320, 340, "Press ENTER to continue or ESC to exit...", 1, 1
		Text 320, 390, "(Use 'W' to move left, 'D' to move right, and 'SPACE' to fire!)", 1, 1
		
		If KeyDown(1) Then End
		
		
		
	Until KeyHit(28)
	
End Function

Function WinScreen()
	
	Repeat
		Cls
		
		ParticleGenerator()
		
		If KeyDown(1) Then End
		Color 12, 255, 0
		Text 320, 240, "YOU WIN!!!", 1, 1
		Text 320, 290, "Press ESC to exit...", 1, 1
		
		
		Flip
		
		
		If KeyHit(28) Then Delay(1000)
	Until KeyHit(28)	
End Function

Function LevelScreen(level)
	
	Repeat
		Cls
		
		ParticleGenerator()
		
		If KeyDown(1) Then End
		Color 12, 255, 0
		Text 320, 240, "LEVEL: " + level, 1, 1
		Text 320, 290, "Press ENTER to continue or ESC to exit...", 1, 1
		
		
		Flip
		
		
		
	Until KeyHit(28)	
End Function


;##############
;# GAME LOOP #
;#############
IntroScreen()
; Start main loop
While Not KeyDown(1)
	; Only draw aliens if there are no aliens
	If numAliens = 0 Then
		FlushKeys
		; Increment the level
		level = level + 1
		counter = counter + 1
		Delay(1000)
		LevelScreen(level)
		If KeyHit(57) Then Delay(1000)
		amx = amx * 1.45
		; Generate aliens
		For z = 1 To 6
			For w = 1 To 2
				a.alien = New alien
				a\x = 100 + (65 * z)
				a\y = 10 + (80 * w)
			Next	
		Next
		
	EndIf
	
	; Clear the screen
	Cls
	
	; Draw background
	DrawImage bkgd_space, 0, 0
	
	; Draw the player
	isEdge = False
	DrawImage img_ship, x, y
	
	; Update player position
	
	If KeyDown(30) Or KeyDown(203) Then 
		x = x - 3
		If x < 0 Then
			x = 640
		EndIf
	EndIf
	
	If KeyDown(32) Or KeyDown(205) And x <> 640 Then
		x = x + 3
		If x >= 640 Then
			x = 1
		EndIf
	EndIf
	
	; Fire bullet
	If KeyHit(57) Then 
		PlaySound(snd_shoot)
		b.bullet = New bullet
		b\x = x - 3
		b\y = y - 85
	EndIf	
	
	; Update bullets and draw them
	For b.bullet = Each bullet
		b\y = b\y - 5
		DrawImage img_bullet, b\x, b\y
		If b\y < 0 Or numAliens = 0 Then Delete b
	Next
	
	If chdir = True Then
		amx = -amx
	EndIf
	chdir = False
	
	numAliens = 0
	
	; Update and draw aliens
	For a.alien = Each alien
		; Count aliens
		numAliens = numAliens + 1
		; Move aliens along x-axis
		a\x = a\x + amx
		; Change direction when edge-of-screen reached
		If a\x > 620 Then chdir = True
		If a\x < 20 Then chdir = True
		
		; Generate enemy bombs
		If Rand(200) = 25 Then 
			PlaySound(snd_bomb)
			bombs.bomb = New bomb
			bombs\x = a\x
			bombs\y = a\y
		EndIf
		
		DrawImage img_alien, a\x, a\y
		For b.bullet = Each bullet
			; Bullet/alien collision code
			If ImagesCollide(img_bullet, b\x, b\y, 0, img_alien, a\x, a\y, 0)
				PlaySound(snd_explode)
				DrawImage(img_fire, a\x, a\y)
				; Delete it all
				Delete b
				Delete a
				
				score = score + 10
				; Set new high score if met
				If score > highScore Then
					highScore = score
				EndIf
				Exit
			EndIf	
		Next	
	Next
	
	
	; Update and draw bombs
	For bombs.bomb = Each bomb
		bombs\y = bombs\y + 4
		
		DrawImage img_bomb, bombs\x, bombs\y
		; If a bomb hits player, decrease lives by 1
		If ImagesCollide(img_bomb, bombs\x, bombs\y, 0, img_ship, x, y, 0)
			PlaySound(snd_shipHit)
			lives = lives - 1
			Delete bombs
			Exit
		ElseIf bombs\y > 500 Then 
			Delete bombs
		EndIf
	Next
	
	; Game Over screen
	If lives = 0 Then
		
		; Save high score
		file_score = WriteFile("score.dat")
		WriteInt(file_score, highScore)
		CloseFile(file_score)
		; Game over
		FlushKeys
		Cls
		ClearWorld()
		EndGraphics()
		StopChannel chnWav
		Graphics 640, 480, 32, 2
		myFont = LoadFont("font/Bitsumishi.ttf", 24, True, 0, 0)
		SetFont myFont
		Color 12, 255, 0
		Text 320, 240, "GAME OVER!", 1, 1
		Delay(5000)
		Goto thetop
		
	EndIf 
	
	; Win screen
	If level = 11 Then WinScreen()
	
	; Draw lives, current score, and high score
	Text 50, 420, "LIVES: " + lives
	Text 50, 440, "SCORE: " + score
	Text 500, 420, "LEVEL: " + level 
	Text 500, 440, "HIGH SCORE: " + highScore
	
	; Wait for the game timer
	WaitTimer(timer)
	
	; Flip buffer to active screen
	Flip	
; End main loop
Wend	

; Wait for key
WaitKey

; End program
End 
;~IDEal Editor Parameters:
;~F#44#4A#50#56
;~C#Blitz3D