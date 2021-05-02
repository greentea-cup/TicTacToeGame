package com.greentea.triplet;

public class Board {
    public final int rowLength;
    public final int lineCount;
    public final int size;
    private final int _strikeLength;
    private final Boolean[] _board;
    private final String _player1;
    private final String _player2;
    private int _turns;
    private boolean _isOtherPlayerTurn;
    private boolean _finished;
    private boolean _isOtherWinner;
    private boolean _hasWinner;

    private Board(int rows, int lines, String player1, String player2, int strikeLength) {
        if (rows < 1 || lines < 1) throw new IllegalArgumentException("invalid board size");
        rowLength = rows;
        lineCount = lines;
        size = rows * lines;
        _player1 = player1;
        _player2 = player2;
        if (strikeLength < 1 || strikeLength > rows || strikeLength > lines)
            throw new IllegalArgumentException("strike length must be > 1 and <= min(rows, lines)");
        _strikeLength = strikeLength;
        _board = new Boolean[size];
        for (int i = 0; i < size; i++) _board[i] = null;
        _turns = 0;
        _isOtherPlayerTurn = false;
        _finished = false;
        _isOtherWinner = false;
        _hasWinner = false;
    }

    public static Builder newBoard() {
        return new Builder();
    }

    public String getBoard(String rowJoiner, String lineJoiner, String unmodifiedCell) {
        StringBuilder builder = new StringBuilder();
        Boolean isOtherPlayer;
        String cellValue, joiner;
        int lineEnd = rowLength - 1;
        for (int i = 0; i < size; i++) {
            isOtherPlayer = _board[i];
            cellValue = (isOtherPlayer == null) ? unmodifiedCell : getPlayer(isOtherPlayer);
            joiner = i % rowLength == lineEnd ? lineJoiner : rowJoiner;
            builder.append(cellValue).append(i == size - 1 ? "" : joiner);
        }
        return builder.toString();
    }

    public String getBoard() {
        return getBoard(" ", "\n", "-");
    }

    @Override
    public String toString() {
        return getBoard();
    }

    public String getCurrentPlayer() {
        return getPlayer(_isOtherPlayerTurn);
    }

    public int getTurnCount() {
        return _turns;
    }

    public boolean hasWinner() {
        return _hasWinner;
    }

    public String tryGetWinner() {
        validateFinished(true);
        validateHasWinner();
        return getPlayer(_isOtherWinner);
    }

    public boolean isCellPlaceable(int x, int y) {
        return isValidPosition(x, y) && isCellPlaceable(getOffset(x, y));
    }

    public boolean isFinished() {
        return _finished;
    }

    public void tryPlaceCell(int x, int y) {
        validateFinished(false);
        validatePosition(x, y);
        int offset = getOffset(x, y);
        if (!isCellPlaceable(offset)) throw new IllegalStateException("cell(" + x + ";" + y + ") is already placed");
        placeCell(offset);
    }

    private String getPlayer(boolean other) {
        return other ? _player2 : _player1;
    }

    private int getOffset(int x, int y) {
        return y * rowLength + x;
    }

    private boolean isCellPlaceable(int offset) {
        return isValidOffset(offset) && _board[offset] == null;
    }

    private boolean hasEmptyCells() {
        for (Boolean b : _board) if (b == null) return true;
        return false;
    }

    private void placeCell(int offset) {
        _board[offset] = _isOtherPlayerTurn;
        makeTurn();
    }

    private boolean isValidOffset(int offset) {
        return offset >= 0 && offset < size;
    }

    private void validatePosition(int x, int y) {
        if (!isValidPosition(x, y)) throw new IllegalArgumentException("invalid cell position");
    }

    private boolean isValidPosition(int x, int y) {
        return x >= 0 && y >= 0 && x < rowLength && y < lineCount;
    }

    private void validateFinished(boolean needFinished) {
        if (_finished != needFinished)
            throw new IllegalStateException("game" + (needFinished ? " not" : "") + " finished");
    }

    private void validateHasWinner() {
        if (_finished && !_hasWinner) throw new IllegalStateException("game has no winner");
    }

    private void makeTurn() {
        _turns++;
        if (checkFinished()) finish(true);
        else if (!hasEmptyCells()) finish(false);
        _isOtherPlayerTurn = !_isOtherPlayerTurn;
    }

    private boolean checkFinished() {
        return (checkHorizontalStrikes() || checkVerticalStrikes() || checkCrossStrikes());
    }

    private boolean checkHorizontalStrikes() {
        int lineEnd = lineCount;
        for (int i = 0; i < lineCount; i++, lineEnd += lineCount) {
            for (int j = 0; j < rowLength; j++) {
                if (rowLength - j < _strikeLength) return false;
                Boolean startCell = _board[i * rowLength + j];
                if (startCell != null) {
                    int strike = 0;
                    for (int k = j; k < lineEnd; k++) {
                        if (startCell.equals(_board[i * rowLength + k])) {
                            strike++;
                            if (strike == _strikeLength) return true;
                        } else break;
                    }
                }
            }
        }
        return false;
    }

    private boolean checkVerticalStrikes() {
        for (int i = 0; i < lineCount; i++) {
            if (lineCount - i < _strikeLength) return false;
            for (int j = 0; j < rowLength; j++) {
                Boolean startCell = _board[i * rowLength + j];
                if (startCell != null) {
                    int strike = 0;
                    for (int k = i; k < lineCount; k++) {
                        if (startCell.equals(_board[k * rowLength + j])) {
                            strike++;
                            if (strike == _strikeLength) return true;
                        } else break;
                    }
                }
            }
        }
        return false;
    }

    private boolean checkCrossStrikes() {
        for (int i = 0; i < lineCount; i++) {
            for (int j = 0, j1 = rowLength - 1; j < rowLength || j1 >= 0; j++, j1--) {
                if (rowLength - j < _strikeLength && j1 < _strikeLength) break;
                Boolean startCell = _board[i * rowLength + j];
                Boolean startCell1 = _board[i * rowLength + j1];
                if (startCell != null) {
                    int strike = 0;
                    for (int k = i, l = j; k < lineCount && l < rowLength; k++, l++) {
                        if (startCell.equals(_board[k * rowLength + l])) {
                            strike++;
                            if (strike == _strikeLength) return true;
                        } else break;
                    }
                }
                if (startCell1 != null) {
                    int strike1 = 0;
                    for (int k1 = i, l1 = j1; k1 < lineCount && l1 >= 0; k1++, l1--) {
                        if (startCell1.equals(_board[k1 * rowLength + l1])) {
                            strike1++;
                            if (strike1 == _strikeLength) return true;
                        } else break;
                    }
                }
            }
        }
        return false;
    }

    private void finish(boolean hasWinner) {
        this._finished = true;
        this._isOtherWinner = _isOtherPlayerTurn;
        this._hasWinner = hasWinner;
    }

    public static final class Builder {
        private int _rowLength;
        private int _lineCount;
        private String _player1;
        private String _player2;
        private int _strikeLength;

        private Builder() {
            _rowLength = 3;
            _lineCount = 3;
            _player1 = "X";
            _player2 = "O";
            _strikeLength = 3;
        }

        private void checkGreaterZero(int value, String type) {
            if (value <= 0) throw new IllegalArgumentException(type + " must be greater than 0");
        }

        public Builder rows(int newRowLength) {
            checkGreaterZero(newRowLength, "Size");
            _rowLength = newRowLength;
            return this;
        }

        public Builder lines(int newLineCount) {
            checkGreaterZero(newLineCount, "Size");
            _lineCount = newLineCount;
            return this;
        }

        public Builder size(int newRowLength, int newLineCount) {
            return this.rows(newRowLength).lines(newLineCount);
        }

        private void checkBlankPlayer(String value) {
            if (value == null || value.isEmpty() || value.isBlank())
                throw new IllegalArgumentException("Players should be valid visible char sequences");
        }

        public Builder player1(String newPlayer1) {
            checkBlankPlayer(newPlayer1);
            _player1 = newPlayer1;
            return this;
        }

        public Builder player2(String newPlayer2) {
            checkBlankPlayer(newPlayer2);
            _player2 = newPlayer2;
            return this;
        }

        public Builder players(String newPlayer1, String newPlayer2) {
            return this.player1(newPlayer1).player2(newPlayer2);
        }

        public Builder strikeLength(int newStrikeLength) {
            checkGreaterZero(newStrikeLength, "Strike length");
            if (newStrikeLength > _rowLength || newStrikeLength > _lineCount)
                throw new IllegalArgumentException("Strike length must be lower or equal to the lowest board size");
            _strikeLength = newStrikeLength;
            return this;
        }

        public Board build() {
            return new Board(_rowLength, _lineCount, _player1, _player2, _strikeLength);
        }
    }
}