Runtime image assets (optional; missing files use Java2D):

- `images/background.png`: title/menu background, centre-cropped cover.
- `avatars/avatar_0.png` through `avatars/avatar_7.png`: 128 x 128 avatar images.

`build.ps1` and `run.bat` copy this directory's contents into `bin/` after javac.
Use `java -cp bin game.Main`. javac alone does not copy resources.
No background or avatar bitmap has been supplied yet.
