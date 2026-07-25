import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../shared/auth.service';

@Component({
  selector: 'app-placement-test',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './placement-test.component.html',
  styleUrls: ['./placement-test.component.css']
})
export class PlacementTestComponent implements OnInit {
  questions = [
    { text: "I ___ a student.", options: ["am", "is", "are", "be"], answer: 0 },
    { text: "She ___ to school every day.", options: ["go", "goes", "going", "gone"], answer: 1 },
    { text: "They ___ watching TV now.", options: ["is", "am", "are", "be"], answer: 2 },
    { text: "We ___ to the cinema yesterday.", options: ["go", "goes", "went", "gone"], answer: 2 },
    { text: "He ___ finished his homework yet.", options: ["hasn't", "haven't", "didn't", "don't"], answer: 0 },
    { text: "If I ___ you, I would study harder.", options: ["was", "am", "were", "be"], answer: 2 },
    { text: "The book ___ written by J.K. Rowling.", options: ["is", "was", "has", "did"], answer: 1 },
    { text: "I look forward to ___ from you.", options: ["hear", "hearing", "heard", "hears"], answer: 1 },
    { text: "She is interested ___ learning English.", options: ["on", "at", "in", "of"], answer: 2 },
    { text: "I have been living here ___ 2010.", options: ["since", "for", "from", "in"], answer: 0 },
    { text: "By the time we arrived, the movie ___.", options: ["started", "has started", "had started", "was starting"], answer: 2 },
    { text: "I wish I ___ a car.", options: ["have", "had", "will have", "would have"], answer: 1 },
    { text: "She asked me where ___.", options: ["was I going", "I was going", "am I going", "I am going"], answer: 1 },
    { text: "Despite ___ tired, he continued working.", options: ["he was", "of being", "being", "to be"], answer: 2 },
    { text: "You ___ smoke in the hospital.", options: ["mustn't", "don't have to", "needn't", "couldn't"], answer: 0 },
    { text: "This is the house ___ I was born.", options: ["which", "that", "where", "when"], answer: 2 },
    { text: "I'll call you as soon as I ___.", options: ["will arrive", "arrive", "arrived", "am arriving"], answer: 1 },
    { text: "It's time we ___ home.", options: ["go", "went", "have gone", "will go"], answer: 1 },
    { text: "I'd rather you ___ do that.", options: ["don't", "didn't", "not", "won't"], answer: 1 },
    { text: "No sooner ___ I arrived than he left.", options: ["did", "have", "had", "was"], answer: 2 },
    { text: "Seldom ___ such a beautiful view.", options: ["I have seen", "have I seen", "I saw", "did I see"], answer: 1 },
    { text: "Not only ___ smart, but also kind.", options: ["she is", "is she", "she was", "was she"], answer: 1 },
    { text: "Had I known, I ___ you.", options: ["would help", "would have helped", "will help", "helped"], answer: 1 },
    { text: "It is essential that he ___ present.", options: ["is", "be", "was", "will be"], answer: 1 },
    { text: "The more you study, the ___ you learn.", options: ["most", "more", "better", "best"], answer: 1 },
    { text: "___ the rain, we went out.", options: ["Despite", "Although", "Even though", "In spite"], answer: 0 },
    { text: "He is believed ___ the richest man.", options: ["is", "to be", "being", "was"], answer: 1 },
    { text: "Let's go, ___?", options: ["will we", "shall we", "do we", "don't we"], answer: 1 },
    { text: "I prefer tea ___ coffee.", options: ["than", "to", "over", "from"], answer: 1 },
    { text: "She is used to ___ up early.", options: ["get", "getting", "got", "gets"], answer: 1 }
  ];

  currentQuestionIndex = 0;
  score = 0;
  selectedOption: number | null = null;
  finished = false;

  constructor(
    private http: HttpClient, 
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit() {
    // If score exists, don't allow retaking
    const storedScore = localStorage.getItem('placementScore');
    if (storedScore !== null) {
      this.router.navigate(['/']);
    }
  }

  selectOption(index: number) {
    this.selectedOption = index;
  }

  nextQuestion() {
    if (this.selectedOption === this.questions[this.currentQuestionIndex].answer) {
      this.score++;
    }
    this.selectedOption = null;
    this.currentQuestionIndex++;
    if (this.currentQuestionIndex >= this.questions.length) {
      this.finishTest();
    }
  }

  finishTest() {
    this.finished = true;
    const username = localStorage.getItem('username');
    if (username) {
        this.http.post<any>('http://localhost:8081/users/placement', {
            username: username,
            score: this.score
        }).subscribe({
            next: (res) => {
              console.log("Score submitted", res);
              this.authService.updateUserInfo(res.levelId, res.placementTestScore);
            },
            error: (err) => console.error("Error submitting score", err)
        });
    }
  }

  goHome() {
    this.router.navigate(['/']);
  }
}
