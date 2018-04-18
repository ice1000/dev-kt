package org.jetbrains.kotlin.com.intellij.lexer;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class LayeredLexer extends DelegateLexer {
	public static ThreadLocal<Boolean> ourDisableLayersFlag = new ThreadLocal<>();
	private int myState;
	private final Map<IElementType, Lexer> myStartTokenToLayerLexer = new HashMap<>();
	private Lexer myCurrentLayerLexer;
	private IElementType myCurrentBaseTokenType;
	private int myLayerLeftPart = -1;
	private int myBaseTokenEnd = -1;
	private final HashSet<Lexer> mySelfStoppingLexers = new HashSet<>(1);
	private final HashMap<Lexer, IElementType[]> myStopTokens = new HashMap<>(1);

	public LayeredLexer(Lexer baseLexer) {
		super(baseLexer);
	}

	public void registerSelfStoppingLayer(Lexer lexer, IElementType[] startTokens, IElementType[] stopTokens) {
		if (!Boolean.TRUE.equals(ourDisableLayersFlag.get())) {
			this.registerLayer(lexer, startTokens);
			this.mySelfStoppingLexers.add(lexer);
			this.myStopTokens.put(lexer, stopTokens);
		}
	}

	public void registerLayer(Lexer lexer, IElementType... startTokens) {
		if (!Boolean.TRUE.equals(ourDisableLayersFlag.get())) {
			for (IElementType startToken : startTokens) this.myStartTokenToLayerLexer.put(startToken, lexer);
		}
	}

	private void activateLayerIfNecessary() {
		IElementType baseTokenType = super.getTokenType();
		this.myCurrentLayerLexer = this.findLayerLexer(baseTokenType);
		if (this.myCurrentLayerLexer != null) {
			this.myCurrentBaseTokenType = baseTokenType;
			this.myBaseTokenEnd = super.getTokenEnd();
			this.myCurrentLayerLexer.start(super.getBufferSequence(), super.getTokenStart(), super.getTokenEnd());
			if (this.mySelfStoppingLexers.contains(this.myCurrentLayerLexer)) {
				super.advance();
			}
		}

	}

	@Nullable
	protected Lexer findLayerLexer(IElementType baseTokenType) {
		return this.myStartTokenToLayerLexer.get(baseTokenType);
	}

	public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
		this.myState = initialState;
		this.myCurrentLayerLexer = null;
		super.start(buffer, startOffset, endOffset, initialState);
		this.activateLayerIfNecessary();
	}

	public int getState() {
		return this.myState;
	}

	public IElementType getTokenType() {
		if (this.isInLayerEndGap()) {
			return this.myCurrentBaseTokenType;
		} else {
			return this.isLayerActive() ? this.myCurrentLayerLexer.getTokenType() : super.getTokenType();
		}
	}

	public int getTokenStart() {
		if (this.isInLayerEndGap()) {
			return this.myLayerLeftPart;
		} else {
			return this.isLayerActive() ? this.myCurrentLayerLexer.getTokenStart() : super.getTokenStart();
		}
	}

	public int getTokenEnd() {
		if (this.isInLayerEndGap()) {
			return this.myBaseTokenEnd;
		} else {
			return this.isLayerActive() ? this.myCurrentLayerLexer.getTokenEnd() : super.getTokenEnd();
		}
	}

	public void advance() {
		if (this.isInLayerEndGap()) {
			this.myLayerLeftPart = -1;
			this.myState = super.getState();
		} else {
			if (this.isLayerActive()) {
				Lexer activeLayerLexer = this.myCurrentLayerLexer;
				IElementType layerTokenType = activeLayerLexer.getTokenType();
				if (!this.isStopToken(this.myCurrentLayerLexer, layerTokenType)) {
					this.myCurrentLayerLexer.advance();
					layerTokenType = this.myCurrentLayerLexer.getTokenType();
				} else {
					layerTokenType = null;
				}

				if (layerTokenType == null) {
					int tokenEnd = this.myCurrentLayerLexer.getTokenEnd();
					if (!this.mySelfStoppingLexers.contains(this.myCurrentLayerLexer)) {
						this.myCurrentLayerLexer = null;
						super.advance();
						this.activateLayerIfNecessary();
					} else {
						this.myCurrentLayerLexer = null;
						if (tokenEnd != this.myBaseTokenEnd) {
							this.myState = 2048;
							this.myLayerLeftPart = tokenEnd;
							return;
						}
					}
				}
			} else {
				super.advance();
				this.activateLayerIfNecessary();
			}

			this.myState = this.isLayerActive() ? 1024 : super.getState();
		}
	}

	@NotNull
	public LexerPosition getCurrentPosition() {
		return new LexerPositionImpl(this.getTokenStart(), this.getState());
	}

	public void restore(@NotNull LexerPosition position) {
		this.start(this.getBufferSequence(), position.getOffset(), this.getBufferEnd(), position.getState());
	}

	private boolean isStopToken(Lexer lexer, IElementType tokenType) {
		IElementType[] stopTokens = this.myStopTokens.get(lexer);
		if (stopTokens == null) {
			return false;
		} else {
			for (IElementType stopToken : stopTokens) {
				if (stopToken == tokenType) {
					return true;
				}
			}

			return false;
		}
	}

	protected boolean isLayerActive() {
		return this.myCurrentLayerLexer != null;
	}

	@Contract(pure = true)
	private boolean isInLayerEndGap() {
		return this.myLayerLeftPart != -1;
	}
}
