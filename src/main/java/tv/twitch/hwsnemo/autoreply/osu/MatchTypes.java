package tv.twitch.hwsnemo.autoreply.osu;

public class MatchTypes {

	static class Game {
		int getGame_id() {
			return game_id;
		}

		int getTeam_type() {
			return team_type;
		}

		private final int game_id;
		private final int team_type;
	
		Game(int game_id, int team_type) {
			this.game_id = game_id;
			this.team_type = team_type;
		}
		
		@Override
		public int hashCode() {
			return game_id;
		}
		
		@Override
		public boolean equals(Object o) {
			return (o instanceof Game) ? ((Game) o).game_id == this.game_id : false;
		}
	}

	static class Score {
		int getTeam() {
			return team;
		}

		int getScore() {
			return score;
		}

		int getUser_id() {
			return user_id;
		}

		private final int team;
		private final int score;
		private final int user_id;
	
		Score(int team, int score, int user_id) {
			this.team = team;
			this.score = score;
			this.user_id = user_id;
		}
	}

}
