package com.example.teststicker.model

// StickerHistoryModel.kt
class StickerHistoryModel {
    private val undoStack = mutableListOf<StickerAction>()  // Lịch sử Undo
    private val redoStack = mutableListOf<StickerAction>()  // Lịch sử Redo

    // Thêm hành động vào lịch sử
    fun addAction(action: StickerAction) {
        undoStack.add(action)
        redoStack.clear() // Xóa redo khi có hành động mới
    }

    // Thực hiện hành động undo
    fun undo(): StickerAction? {
        return if (undoStack.isNotEmpty()) {
            val action = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(action)
            action
        } else null
    }

    // Thực hiện hành động redo
    fun redo(): StickerAction? {
        return if (redoStack.isNotEmpty()) {
            val action = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(action)
            action
        } else null
    }

    // Xóa toàn bộ lịch sử
    fun clearHistory() {
        undoStack.clear()
        redoStack.clear()
    }
}
