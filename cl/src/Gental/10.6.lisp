(defun make-board ()
	(list 'board 0 0 0 0 0 0 0 0 0))

(defun convert-to-letter (v)
	(cond ((equal v 1) "O")
	      ((equal v 10) "X")
	      (T " ")))

(defun print-row (x y z)
	(format T "~&   ~A | ~A | ~A"
		(convert-to-letter x)
		(convert-to-letter y)
		(convert-to-letter z)))

(defun print-board (board)
	(format T "~%")
	(print-row (nth 1 board) (nth 2 board) (nth 3 board))
	(format T "~&  -----------")
	(print-row (nth 4 board) (nth 5 board) (nth 6 board))
	(format T "~&  -----------")
	(print-row (nth 7 board) (nth 8 board) (nth 9 board))
	(format T "~%~%"))
