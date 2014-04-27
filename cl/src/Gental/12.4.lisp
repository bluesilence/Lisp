(defstruct node
  (name NIL)
  (question NIL)
  (yes-case NIL)
  (no-case NIL))

(setf *node-list* NIL)

(defun init ()
  (setf *node-list* NIL))

(defun add-node (name question yes-case no-case)
  (push (make-node :name name
		   :question question
		   :yes-case yes-case
		   :no-case no-case)
	*node-list*)
  name)

(defun find-node (name)
  (find-if (lambda (node)
		(equal (node-name node) name))
		*node-list*))

(defun process-node (name)
  (let ((node (find-node name)))
	(when node
		(if (yes-or-no-p (node-question node))
			(node-yes-case node)
			(node-no-case node)))))

(defun run ()
  (do ((current-node 'start (process-node current-node)))
	((or
		(not (symbolp current-node))
		(not (find-node current-node)))
		current-node)))

(add-node 'start
	  "Does the engine turn over?"
	  'engine-turns-over
	  'engine-wont-turn-over)

(add-node 'engine-turns-over
	  "Will the engine run for any period of time?"
	  'engine-will-run-briefly
	  'engine-wont-run)

(add-node 'engine-wont-run
	  "Is there gas in the tank?"
	  'gas-in-tank
          "Fill the tank and try starting the engine again.")

(add-node 'engine-wont-turn-over
	  "Do you hear any sound when you turn the key?"
	  'sound-when-turn-key
	  'no-sound-when-turn-key)

(add-node 'no-sound-when-turn-key
	  "Is the battery voltage low?"
	  "Replace the battery."
	  'battery-voltage-ok)

(add-node 'battery-voltage-ok
	  "Are the battery cables dirty or loose?"
	  "Clean the cables and tighten the connections."
	  'battery-cables-good)

(add-node 'engine-will-run-briefly
	  "Does the engine stalls when cold but not when warm?"
	  'cold-idle-speed-at-least-700rpm
	  "Adjust the idle speed.")
