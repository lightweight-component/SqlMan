<script setup>
import HelloWorld from "./components/HelloWorld.vue";
// import img from "./assets/img.jpg";
</script>

<template>
  <div>
    <header class="top">
      <div>
        <ul>
          <li><a href="#">Home</a></li>
          <li><a href="#">Documents</a></li>
          <li><a href="#">Blog</a></li>
          <li><a href="#">Contrat</a></li>
          <li><a href="#">SourceCode</a></li>
        </ul>
        <div class="img">
          <img src="./assets/img.jpg" width="500" alt="" />
          <div>This has nothing to do with the "Dragon Ball";<br /> I just happen to be a fan of this comic.</div>
        </div>
        <div class="text">
          <h1>Welcome to SqlMan!</h1>
          <h2>A easy, small, straight forward Java CRUD Helper.</h2>
          <p>SqlMan is a lightweight wrapper over JDBC. It is NOT an ORM but follows a SQL-first approach. It allows you
            to use pure SQL with IF/forEach and pass Map parameters for queries or executions. The results you receive
            are either 'isOk' indicators and 'Map' or Java Bean object(s). SqlMan enables fast CRUD database operations
            with ZERO dependencies, except for the JDK.
          </p>

          <p>Similar libraries：Spring JDBC Template, Apache Commons DbUtils, Jdbi3.</p>

          <br />
          <br />
          <button class="primary">Getting Start</button> <button>Git Code</button>
          <div class="m">- Current version: v1.0 </div>
        </div>

      </div>
    </header>
    <div class="content main">
      <h1>
        A lightweight Data Access Library for Java.
      </h1>
      <section>
        <h2>Usage:</h2>
        <div></div>
      </section>
      <section>
        <h2>Something I would like to say:</h2>
        <div>
          <p>Looking at backend development as a whole, most of the work revolves around database development (commonly
            referred to as "CRUD Boy"). How we interact with databases is closely related to our development experience.
            The industry frequently discusses the "object-relational impedance mismatch," which lies at the heart of the
            issue: given that we have two different "programming worldviews"—a relational model database (SQL) and a
            programming language (Java, C#, Go)—each with its own methodologies, use cases, and ways of thinking, how
            can they coexist harmoniously and exchange information? Clearly, the "impedance mismatch" is not an easy
            problem to solve.</p>

          <p>Many backend developers try to avoid writing SQL by struggling with various ORM (Object-Relational Mapping)
            solutions. To be frank, this isn't necessary. ORMs may seem to offer high development efficiency on the
            surface, but in reality, they are just layers of abstraction—ultimately leading back to the SQL layer. ORMs,
            in simple terms, transform one problem into another for resolution, completely bypassing SQL. However,
            issues specific to databases, such as join queries, nested subqueries, and complex SQL logic, can hardly be
            perfectly addressed within ORMs—or rather, they are not as straightforward or comfortable to write. Why go
            through the trouble of using cumbersome ORMs when direct SQL is more flexible and efficient? ORMs might find
            themselves stretched beyond their capabilities. These problems fall precisely into the domain where
            relational databases excel. Transferring tasks best suited for relational databases to ORMs, which are less
            adept at handling them, seems rather misguided. The object-oriented paradigm (of which ORMs are a
            derivative) should perhaps temper its ambitions and focus on what it does best. Moreover, SQL itself is
            sufficiently elegant and worthy of deep study.</p>
          <p>The replacement of Hibernate by iBatis/MyBatis is a case in point, proving that writing SQL directly can be
            more flexible and efficient (SQL First/SQL Centric). Later developments like MyBatis Plus's lambda syntax
            somewhat revert to earlier approaches—similar to Microsoft's LINQ, which was widely adopted early on.</p>
          <p>
            Of course, this doesn’t mean completely discarding the beneficial aspects of ORMs, such as:
          </p>

          <ul>
            <li>
              The "object" part of ORMs, which automatically converts a row of data (result set) into an object,
              facilitating business code processing.
            </li>
            <li>
              The readability concerns of SQL compared to programming languages, which depend on developer habits;
              familiarity with SQL naturally enhances readability.

            </li>
            <li>
              Single SQL statements cannot cover everything. Clearly, complex business logic requires collaboration
              between SQL and Java.
            </li>
          </ul>


          <p>
            Refer to: The Disaster of ORM (Object-Relational Mapping).
          </p>

          <p>
            Considering these pros and cons, I boldly propose a clear, simple, and reusable JDBC solution aimed at
            addressing CRUD (Create, Read, Update, Delete) issues within the realm of Java and JDBC. Defining the
            interface between the two domains is crucial. In Java, developers often use the DAO (Data Access Object)
            layer to solve this problem, which is exactly what we are discussing here.

          </p>

        </div>
      </section>
    </div>
    <footer>
      SqlMan, a part of <a href="https://www.cnblogs.com/frankcheung/">AJ-Framework</a>, author: Frank Cheung, mail:
      frank@ajaxjs.com, visit <a href="https://www.cnblogs.com/frankcheung/">my tech blog(In Chinese)</a>.
      <br /> Copyright © 2025 Frank Cheung. All rights reserved.
    </footer>
  </div>
  <!--   <HelloWorld msg="Vite + Vue" /> -->
</template>

<style lang="less" scoped>
.content {
  margin: 0 auto;
  max-width: 1300px;
}

header.top {
  height: 700px;
  width: 100%;
  background-color: #eee;

  &>div {
    position: relative;
    .content();
    height: 100%;

    &>ul {
      padding: 0;
      margin: 0;
      padding-top: 30px;

      li {
        display: inline-block;
        width: 90px;
        text-align: center;
        padding-left: 1%;
        padding-right: 1%;
        border-right: 1px solid gray;

        &:first-child {
          padding-left: 0%;
        }

        &:last-child {
          border-right: none;
        }
      }
    }

    &>.img {
      margin-top: 2%;
      margin-right: 1%;
      float: right;

      div {
        color: #afafaf;
        font-size: 8px;
        text-align: center;
      }
    }

    .text {
      position: absolute;
      top: 25%;
      left: 20px;
      width: 100%;

      h1 {
        text-shadow: 3px 1px 2px #ffffff;
        margin: 0;
        font-size: 2.5em;
      }

      h2 {
        color: #4f5961;
        text-shadow: 3px 1px 2px #ffffff;
      }

      p {
        width: 600px;
      }

      button {
        appearance: none;
        /* 移除浏览器默认样式 */
        // background-color: #007bff;
        border: 1px solid gray;
        padding: 0.5rem 1rem;
        font-size: 1rem;
        font-weight: 400;
        line-height: 1.5;
        border-radius: 0.25rem;
        transition: all 0.3s ease-in-out;
        cursor: pointer;
        color: gray;
        text-align: center;
        text-decoration: none;
        display: inline-block;
        vertical-align: middle;
        margin-right: 1%;
      }

      /* 悬停效果 */
      button:hover {
        opacity: 0.9;
      }

      /* 点击效果 */
      button:active {
        transform: translateY(1px);
        box-shadow: 0 2px 0 rgba(0, 0, 0, 0.1);
      }

      /* 禁用状态 */
      button[disabled] {
        opacity: 0.65;
        cursor: not-allowed;
        pointer-events: none;
      }

      /* 不同类型的按钮样式 */
      button.primary {
        background-color: #007bff;
        border-color: #007bff;
        color: white;
      }

      .m {
        font-size: 0.8em;
        color: #969696;
        margin-top: 1%;
      }
    }
  }
}

.main {
  h1 {
    font-size: 2.3rem;
  }

  p {
    color: gray;
    padding: 5px 4%;
  }
}

footer {
  margin-top: 5%;
  border-top: 1px solid lightgray;
  text-align: center;
  padding: 2%;
  color: gray;
}

.logo {
  height: 6em;
  padding: 1.5em;
  will-change: filter;
  transition: filter 300ms;
}

.logo:hover {
  filter: drop-shadow(0 0 2em #646cffaa);
}

.logo.vue:hover {
  filter: drop-shadow(0 0 2em #42b883aa);
}
</style>
