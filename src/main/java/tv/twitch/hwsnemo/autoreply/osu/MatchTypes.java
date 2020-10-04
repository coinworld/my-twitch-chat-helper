package tv.twitch.hwsnemo.autoreply.osu;

public class MatchTypes {

	static class Game {
		private final int game_id;

		private final int team_type;

		Game(int game_id, int team_type) {
			this.game_id = game_id;
			this.team_type = team_type;
		}
		@Override
		public boolean equals(Object o) {
			return (o instanceof Game) ? ((Game) o).game_id == this.game_id : false;
		}
	
		int getGame_id() {
			return game_id;
		}
		
		int getTeam_type() {
			return team_type;
		}
		
		@Override
		public int hashCode() {
			return game_id;
		}
	}

	static class Score {
		private final int team;

		private final int score;

		private final int user_id;

		Score(int team, int score, int user_id) {
			this.team = team;
			this.score = score;
			this.user_id = user_id;
		}
		int getScore() {
			return score;
		}
		int getTeam() {
			return team;
		}
	
		int getUser_id() {
			return user_id;
		}
	}

}
